import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { getAuth } from 'firebase/auth';
import { Separator } from '@radix-ui/react-separator';
import { XCircle } from 'lucide-react';
import GameBoard from './GameBoard';
import ResourceCardsDisplay from './ResourceCardsDisplay';
import BuildingCosts from './BuildingCosts';
import { getPlayerColor, PLAYER_COLORS } from '../../utils/playerColors';
import { cn } from '../../utils/cn';

interface Player {
  id: number;
  username: string;
  isCurrentUser: boolean;
  elo: number;
  totalGames: number;
  totalWins: number;
  resources?: {
    [key: string]: number;
  };
  color?: string;
  devCards: {
    knight: number;
    yearOfPlenty: number;
    monopoly: number;
    roadBuilding: number;
    victoryPoint: number;
  };
}

type ActionType = 'SETTLEMENT' | 'CITY' | 'ROAD' | 'TRADE' | 'DEV_CARD' | null;

interface BoardState {
  hexes: {
    id: number;
    type: 'desert' | 'wood' | 'brick' | 'ore' | 'wheat' | 'sheep';
    number?: number;
    hasRobber: boolean;
    x: number;
    y: number;
  }[];
  vertices: {
    id: number;
    x: number;
    y: number;
    settlement?: {
      playerId: number;
      type: 'settlement' | 'city';
    };
  }[];
  edges: {
    id: string;
    v1: number;
    v2: number;
    road?: {
      playerId: number;
    };
  }[];
}

interface GameData {
  id: number;
  gameName: string;
  inProgress: boolean;
  isGameOver: boolean;
  playerList: number[];
  players?: any[]; // not very strict typing
  currentTurnPlayerId?: number | null;
  setupPhaseComplete?: boolean;
}

const GameRoom = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const auth = getAuth();
  const [players, setPlayers] = useState<Player[]>([]);
  const [currentTurn, setCurrentTurn] = useState<number | null>(null);
  const [gameData, setGameData] = useState<any | null>(null);
  const [selectedAction, setSelectedAction] = useState<ActionType>(null);
  const [showTradeModal, setShowTradeModal] = useState(false);
  const [devCards, setDevCards] = useState<{
    knight: number;
    yearOfPlenty: number;
    monopoly: number;
    roadBuilding: number;
    victoryPoint: number;
  }>({
    knight: 0,
    yearOfPlenty: 0,
    monopoly: 0,
    roadBuilding: 0,
    victoryPoint: 0
  });
  const [boardState, setBoardState] = useState<any | null>(null);
  const [currentAccountId, setCurrentAccountId] = useState<number | null>(null);

  // DERIVED STATE for setup phase
  // TODO: wht is going on here
  const actualIsSetupPhase = gameData && gameData.inProgress && !gameData.setupPhaseComplete;
  const gameIsTrulyInProgress = gameData && gameData.inProgress; // more general flag for "game has started and not over"

  const processPlayersData = (
    playersArray: any[], 
    playerList: number[], // ordered list of player account IDs
    currentUserEmail: string | null | undefined
  ) => {
    return playersArray.map((p: any) => {
      const playerOrder = playerList.indexOf(p.id) + 1; // p.id is accountId. Find its 1-based order.
      return {
        ...p,
        isCurrentUser: p.email === currentUserEmail,
        color: getPlayerColor(playerOrder > 0 ? playerOrder : undefined), // Use playerOrder for color
        devCards: p.devCards ? {
          knight: p.devCards.knight || 0,
          yearOfPlenty: p.devCards.yearOfPlenty || 0,
          monopoly: p.devCards.monopoly || 0,
          roadBuilding: p.devCards.roadBuilding || 0,
          victoryPoint: p.devCards.victoryPoint || 0,
        } : {
          knight: 0,
          yearOfPlenty: 0,
          monopoly: 0,
          roadBuilding: 0,
          victoryPoint: 0,
        }
      };
    });
  };

  useEffect(() => {
    console.log('[DEBUG] GameRoom mounted with gameId:', gameId);
    const currentUser = auth.currentUser;
    if (!currentUser) {
      navigate('/signin');
      return;
    }

    const fetchInitialData = async () => {
      console.log('[DEBUG] Fetching initial game data and user account for gameId:', gameId);
      try {
        const token = await currentUser.getIdToken();
        const headers = { 'Authorization': `Bearer ${token}` };
        
        // fetch game details
        const gameResponse = await fetch(`http://localhost:8080/api/games/${gameId}`, { headers });
        console.log('[DEBUG] Game fetch response status:', gameResponse.status);
        if (!gameResponse.ok) {
          console.error('[ERROR] Failed to fetch game:', gameResponse);
          throw new Error('Failed to fetch game data');
        }
        const gameDetails = await gameResponse.json();
        console.log('[DEBUG] Fetched game data (initial):', gameDetails);
        setGameData(gameDetails.game);
        if (gameDetails.game && gameDetails.game.currentTurnPlayerId !== undefined) {
          setCurrentTurn(gameDetails.game.currentTurnPlayerId);
          console.log('[DEBUG] Initial currentTurn set from gameDetails.game.currentTurnPlayerId:', gameDetails.game.currentTurnPlayerId);
        } else {
          console.log('[DEBUG] Initial currentTurnPlayerId not present in gameDetails.game.');
        }

        if (gameDetails.boardState) {
          setBoardState(gameDetails.boardState);
        } else {
          setBoardState(null);
        }

        // fetch current user's account details to get numeric ID
        const accountResponse = await fetch(`http://localhost:8080/api/account/by-email`, {
          method: 'POST',
          headers: { ...headers, 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: currentUser.email })
        });
        if (accountResponse.ok) {
          const accountData = await accountResponse.json();
          console.log('[DEBUG] Fetched current user account data:', accountData);
          setCurrentAccountId(accountData.id); // store the numeric account ID
          // process players after getting current user's account ID to correctly set isCurrentUser
          const fetchedPlayers = gameDetails.game.players || [];
          const orderedPlayerIds = gameDetails.game.playerList || []; // Get playerList
          setPlayers(processPlayersData(fetchedPlayers, orderedPlayerIds, currentUser.email)); // Pass orderedPlayerIds
        } else {
          console.error('[ERROR] Failed to fetch current user account data.');
        }

      } catch (error) {
        console.error('[ERROR] Error fetching initial data:', error);
      }
    };

    fetchInitialData();



  }, [auth, navigate, gameId]);

  useEffect(() => {

    if (gameId && (!gameData || !gameData.isGameOver)) {
      const interval = setInterval(() => {
        fetchGameState().catch(error => {
          console.error('[ERROR] Game state polling failed:', error);
        });
      }, 3000); // poll every 3 seconds

      return () => {
        clearInterval(interval);
      };
    }
  }, [gameId, gameData]); // depend on gameData to re-evaluate polling when game status changes

  // new useEffect to sync current user's dev cards
  useEffect(() => {
    const currentUserData = players.find(p => p.isCurrentUser);
    if (currentUserData && currentUserData.devCards) {
      setDevCards(currentUserData.devCards);
    } else {
      // reset if no current user or no dev cards for them, or if players array is empty
      setDevCards({
        knight: 0,
        yearOfPlenty: 0,
        monopoly: 0,
        roadBuilding: 0,
        victoryPoint: 0
      });
    }
  }, [players]); // re-run when players array changes (which happens after initial fetch and polling)

  const fetchGameState = async () => {
    if (!auth.currentUser) {
      console.warn('[WARN] User not authenticated, skipping fetchGameState');
      return; 
    }
    try {
      const token = await auth.currentUser.getIdToken();
      const headers = { 'Authorization': `Bearer ${token}` }; // Add auth header for polling too
      const response = await fetch(`http://localhost:8080/api/games/${gameId}`, { headers });
      
      if (!response.ok) {
        console.error('[ERROR] Failed to fetch polled game state:', {
          status: response.status,
          statusText: response.statusText,
          url: response.url
        });

        return; 
      }

      const data = await response.json();
      
      if (data.game) {
        setGameData((prevGameData: GameData | null) => {
          if (JSON.stringify(prevGameData) !== JSON.stringify(data.game)) {
            return data.game;
          }
          return prevGameData;
        });

        const fetchedPlayers = data.game.players || [];
        const currentUserEmail = auth.currentUser?.email;
        const orderedPlayerIds = data.game.playerList || []; // get playerList from current game data
        const processedPlayers = processPlayersData(fetchedPlayers, orderedPlayerIds, currentUserEmail); // Pass orderedPlayerIds
        setPlayers((prevPlayers: Player[]) => {
          if (JSON.stringify(prevPlayers) !== JSON.stringify(processedPlayers)) {
            return processedPlayers;
          }
          return prevPlayers;
        });
      } else {
      }
      
      if (data.boardState) {
        setBoardState((prevBoardState: BoardState | null) => {
          if (JSON.stringify(prevBoardState) !== JSON.stringify(data.boardState)) {
            return data.boardState;
          }
          return prevBoardState;
        });
      } else if (data.game && !data.game.inProgress && !actualIsSetupPhase) {
        setBoardState((prevBoardState: BoardState | null) => {
          if (prevBoardState !== null) {
            return null;
          }
          return prevBoardState;
        });
      } else if (data.game && !data.game.inProgress && actualIsSetupPhase) {
      }

      // update whose the current turn
      if (data.game && data.game.currentTurnPlayerId !== undefined) {
        setCurrentTurn((prevCurrentTurn: number | null) => {
          if (prevCurrentTurn !== data.game.currentTurnPlayerId) {
            return data.game.currentTurnPlayerId;
          }
          return prevCurrentTurn;
        });
      }
    } catch (error) {
      console.error('[ERROR] Error fetching polled game state:', error);
  
    }
  };

  const handleTurnComplete = () => {
    if (!gameData || currentTurn === null) return;

    // get current player index
    const currentPlayerIndex = gameData.playerList.indexOf(currentTurn);
    let nextPlayerIndex;

    if (actualIsSetupPhase) {
      // TODO: look at this again
      // during setup phase:
      // Backend handles actual setup turn order via setupPlacementOrder and currentSetupPlacementIndex
      // Frontend just needs to signal completion of an action. Backend determines next turn and if setup is over.
      // The complex logic for first/second round can be removed here, as backend response will dictate new currentTurn.
      // We simply expect the backend to update currentTurnPlayerId and setupPhaseComplete.
      // This function might not even be needed if backend response from actions correctly sets new turn.
      // For now, keep a simplified version if strictly client-side turn advancement is desired for UI responsiveness,
      // but it should align with backend truth.
      // The backend /api/games/{gameId}/setup-action now triggers advanceSetupTurn in GameEngine,
      // which updates currentTurnPlayerId and setupPhaseComplete and persists.
      // So, the response from setup-action in GameRoom's handleSetupActionSuccess should update currentTurn and gameData.
      // This handleTurnComplete might be redundant for setup phase or only for non-setup.
      
      // If this is called, it means a setup action was done, and we are waiting for backend to tell us the new state.
      // The 'currentTurn' should be updated via polling or the response of the action itself.
      // The old logic of manually calculating next setup turn player is removed.
      console.log("[GameRoom] handleTurnComplete called during setup. Backend should dictate next turn via action response or polling.");
      // No explicit setIsSetupPhase(false) here; it's derived from gameData.setupPhaseComplete.
      return; // Let action response / polling handle state changes.
    } else {
      // regular game: clockwise rotation
      nextPlayerIndex = (currentPlayerIndex + 1) % gameData.playerList.length;
      setCurrentTurn(gameData.playerList[nextPlayerIndex]); // Update currentTurn for regular phase
    }
  };

  const handleStartGame = async () => {
    console.log('handleStartGame called, gameId:', gameId);
    if (!gameId || !auth.currentUser) {
      console.log('No gameId or current user found, returning');
      return;
    }

    try {
      const token = await auth.currentUser.getIdToken();
      const headers = { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      };

      console.log('Making start game request to:', `http://localhost:8080/api/games/${gameId}/start`);
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/start`, {
        method: 'POST',
        headers: headers
      });

      console.log('Start game response status:', response.status);
      console.log('Start game response headers:', Object.fromEntries(response.headers.entries()));
      
      let responseBody;
      try {
        const textBody = await response.text();
        console.log('Raw response body:', textBody);
        try {
          responseBody = JSON.parse(textBody);
          console.log('Parsed response body:', responseBody);
        } catch (parseError) {
          console.error('Failed to parse response as JSON:', parseError);
          console.log('Non-JSON response body:', textBody);
        }
      } catch (bodyError) {
        console.error('Failed to read response body:', bodyError);
      }

      if (!response.ok) {
        const errorMessage = responseBody?.error || responseBody?.message || 'Unknown error occurred';
        console.error('Start game failed:', {
          status: response.status,
          statusText: response.statusText,
          error: errorMessage,
          fullResponse: responseBody
        });
        throw new Error(`Failed to start game: ${errorMessage}`);
      }

      const data = responseBody;
      console.log('[DEBUG] Start game API call successful, raw response data:', data);

      if (!data || !data.game) {
        console.error('[ERROR] Invalid response data from start game API - missing game object:', data);
        throw new Error('Invalid server response: missing game data');
      }


      console.log(`[DEBUG] Game data from start response - gameId: ${data.game.id}, inProgress: ${data.game.inProgress}, name: ${data.game.gameName}`);

      // update game state
      setGameData(data.game);
      
      // update board state
      if (data.boardState) {
        console.log('[DEBUG] Updating board state with data from start game response.');
        const { hexes, vertices, edges } = data.boardState;
        setBoardState({
          hexes: hexes || [],
          vertices: vertices || [],
          edges: edges || []
        });
      } else {
        console.warn('No board state received in response');
      }


      // set init turn order - host (first player) goes first 
      // can change later to allow shuffling of players
      if (data.game.playerList && data.game.playerList.length > 0) {
        console.log('Setting initial turn to:', data.game.playerList[0]);
        setCurrentTurn(data.game.playerList[0]);
        
      } else {
        console.warn('No player list in game data:', data.game);
      }

    } catch (error: any) {
      console.error('Error in handleStartGame:', {
        error,
        message: error.message,
        stack: error.stack,
        gameId,
        currentGameState: gameData,
        currentPlayers: players
      });
      alert(`Failed to start game: ${error.message || 'Unknown error occurred'}`);
    }
  };


  const handleActionClick = (action: ActionType) => {
    if (selectedAction === action) {
      setSelectedAction(null);
    } else {
      setSelectedAction(action);
      if (action === 'TRADE') {
        setShowTradeModal(true);
      }
    }
  };

  const handleGameAction = async (locationId: string | number) => {
    if (!gameId || !selectedAction || !gameData) return;

    const currentPlayer = players.find(p => p.isCurrentUser);
    if (!currentPlayer) return;

    try {
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/action`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountId: currentPlayer.id,
          action: selectedAction,
          locationId
        })
      });

      if (!response.ok) {
        throw new Error('Failed to perform action');
      }

      const data = await response.json();
      // update player resources and game state
      const updatedPlayers = players.map(p => 
        p.id === currentPlayer.id ? { ...p, ...data.player } : p
      );
      setPlayers(updatedPlayers);
      setSelectedAction(null);
    } catch (error) {
      console.error('Error performing action:', error);
    }
  };

  const handleEndTurn = async () => {
    if (!gameId || !gameData) return;

    const currentPlayer = players.find(p => p.isCurrentUser);
    if (!currentPlayer) return;

    try {
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/end-turn`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountId: currentPlayer.id
        })
      });

      if (!response.ok) {
        throw new Error('Failed to end turn');
      }

      const data = await response.json();
      
      // update game state
      setGameData(data.game);
      
      // if game is over, show winner
      if (data.game.isGameOver) {
        const winner = players.find(p => p.id === data.game.winnerId);
        alert(`Game Over! ${winner?.username} has won the game!`);
        navigate('/lobby');
        return;
      }

      // move to next player's turn
      handleTurnComplete();
    } catch (error) {
      console.error('Error ending turn:', error);
    }
  };

  const handleBuyDevCard = async () => {
    if (!gameId || !gameData) return;

    const currentPlayer = players.find(p => p.isCurrentUser);
    if (!currentPlayer) return;

    try {
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/buy-dev-card`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountId: currentPlayer.id
        })
      });

      if (!response.ok) {
        throw new Error('Failed to buy development card');
      }

      const data = await response.json();
      if (data.devCard) {
        setDevCards(prev => ({
          ...prev,
          [data.devCard]: prev[data.devCard as keyof typeof prev] + 1
        }));
      }
    } catch (error) {
      console.error('Error buying development card:', error);
    }
  };

  const handlePlayDevCard = async (cardType: string) => {
    if (!gameId || !gameData) return;

    const currentPlayer = players.find(p => p.isCurrentUser);
    if (!currentPlayer) return;

    try {
      let additionalParams = {};

      switch (cardType) {
        case 'knight':
          // for Knight card, we need to select a hex and a player to steal from
          const targetHex = window.prompt('Enter the hex number to move the robber to:');
          const targetPlayer = window.prompt('Enter the player ID to steal from:');
          if (!targetHex || !targetPlayer) return;
          additionalParams = {
            targetHex: parseInt(targetHex),
            targetPlayerId: parseInt(targetPlayer)
          };
          break;

        case 'yearOfPlenty':
          // for yop, select 2 resources
          const resource1 = window.prompt('Select first resource (brick, wood, ore, wheat, sheep):');
          const resource2 = window.prompt('Select second resource (brick, wood, ore, wheat, sheep):');
          if (!resource1 || !resource2) return;
          additionalParams = { resource1, resource2 };
          break;

        case 'monopoly':
          // for mono, select one resource type 
          const resource = window.prompt('Select resource to monopolize (brick, wood, ore, wheat, sheep):');
          if (!resource) return;
          additionalParams = { resource };
          break;

        case 'roadBuilding':
          // for road building, we need to select two edges
          const edge1 = window.prompt('Enter the first edge ID:');
          const edge2 = window.prompt('Enter the second edge ID:');
          if (!edge1 || !edge2) return;
          additionalParams = {
            edge1: parseInt(edge1),
            edge2: parseInt(edge2)
          };
          break;
      }

      const response = await fetch(`http://localhost:8080/api/games/${gameId}/play-dev-card`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountId: currentPlayer.id,
          cardType,
          ...additionalParams
        })
      });

      if (!response.ok) {
        throw new Error('Failed to play development card');
      }

      const data = await response.json();
      // update dev cards count
      setDevCards(prev => ({
        ...prev,
        [cardType]: prev[cardType as keyof typeof prev] - 1
      }));
      
      // update game state if needed
      if (data.game) {
        setGameData(data.game);
      }
    } catch (error) {
      console.error('Error playing development card:', error);
    }
  };

  const handleSetupActionSuccess = (responseData: any) => {
    console.log('[GameRoom] Setup action successful, updating state:', responseData);
    if (responseData.game) {
      setGameData({ 
        ...responseData.game,
        playerList: [...(responseData.game.playerList || [])],
        players: responseData.game.players ? responseData.game.players.map((p: any) => ({...p})) : [] 
      });
      const fetchedPlayers = responseData.game.players || [];
      const currentUserEmail = auth.currentUser?.email;
      setPlayers(processPlayersData(fetchedPlayers, responseData.game.playerList || [], currentUserEmail));
    }
    if (responseData.boardState) {
      setBoardState({
        ...responseData.boardState,
        vertices: responseData.boardState.vertices ? responseData.boardState.vertices.map((v: any) => ({...v})) : [],
        edges: responseData.boardState.edges ? responseData.boardState.edges.map((e: any) => ({...e})) : [],
        hexes: responseData.boardState.hexes ? responseData.boardState.hexes.map((h: any) => ({...h})) : []
      });
    }
    if (responseData.currentTurnPlayerId !== undefined) {
        setCurrentTurn(responseData.currentTurnPlayerId);
        console.log('[GameRoom] Set currentTurn from responseData.currentTurnPlayerId:', responseData.currentTurnPlayerId);
    } else if (responseData.game && responseData.game.currentTurnPlayerId !== undefined) {
        // fallback if it was mistakenly nested under game (less likely based on backend code)
        setCurrentTurn(responseData.game.currentTurnPlayerId);
        console.log('[GameRoom] Set currentTurn from responseData.game.currentTurnPlayerId:', responseData.game.currentTurnPlayerId);
    } else {
        console.warn('[GameRoom] currentTurnPlayerId not found in setup action response.');
    }
  };

  const handleRollDiceClick = async () => {
    if (!gameId || !currentAccountId || currentTurn !== currentAccountId) {
      console.error('[GameRoom] Cannot roll dice: missing gameId, accountId, or not current turn.');
      return;
    }
    const currentUser = auth.currentUser;
    if (!currentUser) {
        console.error("[GameRoom] No current user found on auth object for roll dice.");
        return;
    }
    const token = await currentUser.getIdToken();
    if (!token) {
        console.error("[GameRoom] No auth token found for roll dice.");
        return;
    }

    try {
      console.log(`[GameRoom] Player ${currentAccountId} rolling dice for game ${gameId}.`);
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/roll-dice`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ accountId: currentAccountId }) // send accountId for turn verification
      });

      if (response.ok) {
        const responseData = await response.json();
        console.info('[GameRoom] Dice roll successful, responseData:', responseData);
        // update game state (gameData for dice roll, player resources via boardState/players)
        if (responseData.game) {
          setGameData({
            ...responseData.game,
            playerList: [...(responseData.game.playerList || [])],
            players: responseData.game.players ? responseData.game.players.map((p: any) => ({...p})) : []
          });
           const fetchedPlayers = responseData.game.players || [];
           const currentUserEmail = auth.currentUser?.email;
           setPlayers(processPlayersData(fetchedPlayers, responseData.game.playerList || [], currentUserEmail));
        }
        if (responseData.boardState) {
          setBoardState({
            ...responseData.boardState,
            vertices: responseData.boardState.vertices ? responseData.boardState.vertices.map((v: any) => ({...v})) : [],
            edges: responseData.boardState.edges ? responseData.boardState.edges.map((e: any) => ({...e})) : [],
            hexes: responseData.boardState.hexes ? responseData.boardState.hexes.map((h: any) => ({...h})) : []
          });
        }
        if (responseData.diceRoll) {
            alert(`You rolled: ${responseData.diceRoll.dice1} + ${responseData.diceRoll.dice2} = ${responseData.diceRoll.sum}`);
        }
      } else {
        const errorData = await response.json().catch(() => ({ message: 'Failed to roll dice' }));
        console.error('[GameRoom] Error rolling dice:', response.status, errorData);
        alert(`Error rolling dice: ${errorData.message || response.statusText}`);
      }
    } catch (error) {
      console.error('[GameRoom] Exception during dice roll:', error);
      alert('An exception occurred while rolling the dice.');
    }
  };

  const handleQuitGame = () => {
    // potentially update game state to remove player from player list, for turn management
    // check if the player is in player list, if not, skip their turn
    // if only one player left, crown them the winner automatically. 
    navigate('/lobby');
  };

  const PlayerCard = ({ player, isHost, playerOrder }: { player: Player; isHost: boolean; playerOrder: number }) => {
    // host is the first player in the player list


    return (
    <div 
      className={cn(
        "relative rounded-xl p-4 transition-all duration-200",
        "bg-white/20 backdrop-blur-sm",
        "border-[1.5px] shadow-lg hover:shadow-xl",
        gameData?.inProgress && currentTurn === player.id && "ring-2 ring-offset-2 ring-offset-blue-100/50" // Only ring if game in progress and current turn
      )}
      style={{
        borderColor: getPlayerColor(playerOrder)
      }}
    >
        {/* Badge Logic: Show 'Waiting' if game NOT in progress, Show 'Current Turn' if game IS in progress AND it's this player's turn */}
        {!gameData?.inProgress && (
          <div className="absolute -top-2 -right-2 px-2 py-0.5 bg-gray-400/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
            Waiting to Start
          </div>
        )}
        {gameData?.inProgress && currentTurn === player.id && (
        <div className="absolute -top-2 -right-2 px-2 py-0.5 bg-green-500/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
          Current Turn
            </div>
          )
        }
        {/* Host Badge (independent of game progress) */}
        {isHost && (
          <div className="absolute -top-2 -left-2 px-2 py-0.5 bg-purple-500/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
            Host
        </div>
      )}
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <div 
            className={cn(
              "w-4 h-4 rounded-full border-2",
              "transition-all duration-200"
            )}
            style={{
              backgroundColor: player.isCurrentUser ? 'white' : getPlayerColor(playerOrder),
              borderColor: getPlayerColor(playerOrder)
            }}
          />
          <h3 className="text-base font-semibold text-gray-800">
            {player.username}
          </h3>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-sm font-medium text-gray-500">ELO</span>
          <span className="text-sm font-bold text-gray-700">{player.elo}</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 text-xs text-gray-600 mb-2">
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Games</span>
          <span>{player.totalGames}</span>
        </div>
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Wins</span>
          <span>{player.totalWins}</span>
        </div>
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Rate</span>
          <span>{((player.totalWins / (player.totalGames || 1)) * 100).toFixed(1)}%</span>
        </div>
      </div>

      {/* Replace the existing grid for VP, Knights, Roads with a new styled one */}
      <div className="grid grid-cols-3 gap-2 text-gray-700 mb-3">
        {/* Victory Points */}
        <div className="flex items-center justify-center gap-1 bg-white/30 backdrop-blur-sm rounded-md p-1.5 shadow border border-gray-300/40">
          <span className="font-semibold text-sm text-purple-700">V.P.</span>
          <span className="text-sm font-bold">{player.devCards.victoryPoint}</span>
        </div>
        {/* Knights */}
        <div className="flex items-center justify-center gap-1 bg-white/30 backdrop-blur-sm rounded-md p-1.5 shadow border border-gray-300/40">
          <span className="text-xl">⚔️</span>
          <span className="text-sm font-bold">{player.devCards.knight}</span>
        </div>
        {/* Road Building */}
        <div className="flex items-center justify-center gap-1 bg-white/30 backdrop-blur-sm rounded-md p-1.5 shadow border border-gray-300/40">
          <span className="text-xl">🛣️</span>
          <span className="text-sm font-bold">{player.devCards.roadBuilding}</span>
        </div>
      </div>

      {player.isCurrentUser && player.resources && (
        <>
          <Separator className="my-2 bg-gray-200" />
          <div className="space-y-1">
            <div className="font-medium text-xs text-gray-700 mb-2">Resources</div>
            <ResourceCardsDisplay resources={player.resources} className="justify-start"/>
          </div>
        </>
      )}
    </div>
  );
  };

  const ActionButtons = () => {
    const currentPlayer = players.find(p => p.isCurrentUser);
    const isCurrentPlayerTurn = currentPlayer && currentTurn === currentPlayer.id;

    return (
      <div className="fixed bottom-4 left-1/2 transform -translate-x-1/2 flex flex-col gap-4 items-center z-10">
        {isCurrentPlayerTurn && gameIsTrulyInProgress && (
          actualIsSetupPhase ? (
            <div className="text-lg font-semibold p-2 bg-blue-100/70 rounded-md shadow">Setup Phase: Place pieces</div>
          ) : (
            // mormal game phase buttons
            <>
        <div className="flex flex-wrap justify-center gap-4">
                <button
                  onClick={handleRollDiceClick}
                  className={cn(
                    "w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
                    "bg-white/40 backdrop-blur-sm border border-green-200/50",
                    "hover:bg-green-50/50"
                  )}
                >
                  🎲 Roll Dice
                </button>
          <button
            onClick={() => handleActionClick('SETTLEMENT')}
            className={cn(
              "w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/40 backdrop-blur-sm border border-orange-200/50",
              selectedAction === 'SETTLEMENT' ? "ring-2 ring-orange-400" : "hover:bg-orange-50/50"
            )}
          >
            🏠 Settlement
          </button>
          <button
            onClick={() => handleActionClick('CITY')}
            className={cn(
              "w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/40 backdrop-blur-sm border border-blue-200/50",
              selectedAction === 'CITY' ? "ring-2 ring-blue-400" : "hover:bg-blue-50/50"
            )}
          >
            🏰 City
          </button>
          <button
            onClick={() => handleActionClick('ROAD')}
            className={cn(
              "w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/40 backdrop-blur-sm border border-brown-200/50",
              selectedAction === 'ROAD' ? "ring-2 ring-brown-400" : "hover:bg-brown-50/50"
            )}
          >
            🛣️ Road
          </button>
          <button
            onClick={() => handleActionClick('TRADE')}
            className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all 
                      bg-white/40 backdrop-blur-sm border border-green-200/50 
                      hover:bg-green-50/50"
          >
            🤝 Trade
          </button>
          <button
            onClick={handleBuyDevCard}
            className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all 
                      bg-white/40 backdrop-blur-sm border border-yellow-200/50 
                      hover:bg-yellow-50/50"
          >
                  <span role="img" aria-label="scroll">📜</span> Buy Dev Card 
          </button>
          <button
            onClick={handleEndTurn}
            className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg transition-all 
                      bg-white/40 backdrop-blur-sm border border-purple-200/50 
                      hover:bg-purple-50/50"
          >
            ⏭️ End Turn
          </button>
        </div>

              {/* Development Cards - show if not in setup phase and current turn */}
        {(devCards.knight > 0 || devCards.yearOfPlenty > 0 || devCards.monopoly > 0 || 
                devCards.roadBuilding > 0 || devCards.victoryPoint > 0) &&
          <div className="flex flex-wrap justify-center gap-2 mt-2">
            {devCards.knight > 0 && (
              <button
                onClick={() => handlePlayDevCard('knight')}
                className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg bg-white/40 backdrop-blur-sm 
                          border border-red-200/50 hover:bg-red-50/50 transition-all"
              >
                ⚔️ Knight ({devCards.knight})
              </button>
            )}
            {devCards.yearOfPlenty > 0 && (
              <button
                onClick={() => handlePlayDevCard('yearOfPlenty')}
                className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg bg-white/40 backdrop-blur-sm 
                          border border-green-200/50 hover:bg-green-50/50 transition-all"
              >
                📦 Year of Plenty ({devCards.yearOfPlenty})
              </button>
            )}
            {devCards.monopoly > 0 && (
              <button
                onClick={() => handlePlayDevCard('monopoly')}
                className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg bg-white/40 backdrop-blur-sm 
                          border border-blue-200/50 hover:bg-blue-50/50 transition-all"
              >
                💰 Monopoly ({devCards.monopoly})
              </button>
            )}
            {devCards.roadBuilding > 0 && (
              <button
                onClick={() => handlePlayDevCard('roadBuilding')}
                className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg bg-white/40 backdrop-blur-sm 
                          border border-brown-200/50 hover:bg-brown-50/50 transition-all"
              >
                🛣️ Road Building ({devCards.roadBuilding})
              </button>
            )}
            {devCards.victoryPoint > 0 && (
              <button
                className="w-36 text-center px-4 py-2 rounded-xl font-medium shadow-lg bg-white/40 backdrop-blur-sm 
                          border border-yellow-200/50"
              >
                👑 Victory Point ({devCards.victoryPoint})
              </button>
            )}
          </div>
              }
            </>
          )
        )}
      </div>
    );
  };

  const isCurrentUserHost = gameData && gameData.playerList && gameData.playerList.length > 0 && players.find(p => p.isCurrentUser)?.id === gameData.playerList[0];
  const showStartGameButton = gameData && !gameData.inProgress && !gameData.isGameOver && isCurrentUserHost;

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-slate-200 to-slate-300 flex overflow-hidden">
      {gameData && !gameIsTrulyInProgress && (
      <button
        onClick={handleQuitGame}
        className="fixed top-4 left-4 z-10 flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-red-600 text-sm font-medium rounded-xl hover:bg-red-50/50 transition-all shadow-lg hover:shadow-xl border border-red-200/50"
      >
        <XCircle size={16} />
        <span>Quit Game</span>
      </button>
      )}

      <div className="flex-1 relative">
        <div className="absolute inset-0">
          <div className="text-center py-4 bg-white/10 backdrop-blur-sm border-b border-white/20">
            <h2 className="text-2xl font-bold text-gray-800/90">Game #{gameId}</h2>
          </div>
          <div className="w-full h-[calc(100%-80px)]">
            {gameData && (
              <h2 className="text-2xl font-bold mb-4 text-center">{gameData.gameName}</h2>
            )}
            {boardState && currentAccountId !== null && (
              <GameBoard 
                gameId={Number(gameId)}
                accountId={currentAccountId}
                auth={auth}
                isSetupPhase={actualIsSetupPhase}
                isCurrentTurn={currentTurn === players.find(p => p.isCurrentUser)?.id}
                onPlacementComplete={handleTurnComplete}
                selectedAction={selectedAction}
                onActionSelect={handleGameAction}
                boardState={boardState}
                gameConfig={gameData}
                players={players}
                onSetupActionSuccess={handleSetupActionSuccess}
              />
            )}
          </div>
        </div>
      </div>
      <ActionButtons />

      <div className="w-96 bg-white/10 backdrop-blur-sm border-l border-white/20 p-6 overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800/90">Players</h2>
          {showStartGameButton && (
            <button
              onClick={handleStartGame}
              className="px-4 py-2 bg-green-100/50 text-green-700/90 hover:bg-green-100/60 font-medium rounded-xl transition-all shadow-lg hover:shadow-xl border border-green-200/50 backdrop-blur-sm"
            >
              Start Game
            </button>
          )}
        </div>

        {/* Conditionally render BuildingCosts if game is in progress */}
        {gameIsTrulyInProgress && !actualIsSetupPhase && (
          <div className="mb-6">
            <BuildingCosts />
          </div>
        )}

        <div className="space-y-4">
          {players.map((p, index) => {
            const isHostForThisCard = gameData && gameData.playerList && gameData.playerList.length > 0 && gameData.playerList[0] === p.id;
            return <PlayerCard key={p.id} player={p} isHost={isHostForThisCard} playerOrder={index + 1} />;
          })}
        </div>
      </div>
    </div>
  );
};

export default GameRoom; 