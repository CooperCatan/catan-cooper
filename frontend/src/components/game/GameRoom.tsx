import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAuth } from 'firebase/auth';
import { Separator } from '@radix-ui/react-separator';
import { XCircle } from 'lucide-react';
import GameBoard from './GameBoard';
import { PLAYER_COLORS } from './GameBoard';
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

const GameRoom = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const auth = getAuth();
  const [countdown, setCountdown] = useState(60);
  const [players, setPlayers] = useState<Player[]>([]);
  const [isSetupPhase, setIsSetupPhase] = useState(true);
  const [currentTurn, setCurrentTurn] = useState<number | null>(null);
  const [isCurrentTurn, setIsCurrentTurn] = useState(false);
  const [game, setGame] = useState<any | null>(null);
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
  const [gameData, setGameData] = useState<any | null>(null);
  const [boardState, setBoardState] = useState<any | null>(null);
  const [currentAccountId, setCurrentAccountId] = useState<number | null>(null);

  const processPlayersData = (playersArray: any[], currentUserEmail: string | null | undefined) => {
    return playersArray.map((p: any) => ({
      ...p,
      isCurrentUser: p.email === currentUserEmail,
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
    }));
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
        
        // Fetch game details
        const gameResponse = await fetch(`http://localhost:8080/api/games/${gameId}`, { headers });
        console.log('[DEBUG] Game fetch response status:', gameResponse.status);
        if (!gameResponse.ok) {
          console.error('[ERROR] Failed to fetch game:', gameResponse);
          throw new Error('Failed to fetch game data');
        }
        const gameDetails = await gameResponse.json();
        console.log('[DEBUG] Fetched game data (initial):', gameDetails);
        setGameData(gameDetails.game);
        if (gameDetails.boardState) {
          setBoardState(gameDetails.boardState);
        } else {
          setBoardState(null);
        }
        // setGame(gameDetails.game); // Keep if old 'game' state is used elsewhere, else remove

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
          setPlayers(processPlayersData(fetchedPlayers, currentUser.email)); // email for isCurrentUser logic
        } else {
          console.error('[ERROR] Failed to fetch current user account data.');
           // handle case where account might not exist in DB yet if it's a new user straight to game room
           // should not hapen bc the gameLobby, gameRoom, etc is a protected path
        }

      } catch (error) {
        console.error('[ERROR] Error fetching initial data:', error);
      }
    };

    fetchInitialData();

    // set up countdown timer -- unused
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      clearInterval(timer);
    };
  }, [auth, navigate, gameId]);

  useEffect(() => {
    console.log('[DEBUG] Setting up game state polling, gameData:', gameData);
    // poll if gameId exists and game is not over (or gameData is not yet loaded)
    if (gameId && (!gameData || !gameData.isGameOver)) {
      const interval = setInterval(() => {
        console.log('[DEBUG] Polling game state...');
        fetchGameState().catch(error => {
          console.error('[ERROR] Game state polling failed:', error);
        });
      }, 5000); // poll every 5 seconds

      return () => {
        console.log('[DEBUG] Cleaning up game state polling');
        clearInterval(interval);
      };
    }
  }, [gameId, gameData]); // depend on gameData to re-evaluate polling when game status changes

  const fetchGameState = async () => {
    console.log('[DEBUG] Fetching game state for gameId:', gameId);
    if (!auth.currentUser) {
      console.warn('[WARN] User not authenticated, skipping fetchGameState');
      return; 
    }
    try {
      const token = await auth.currentUser.getIdToken();
      const headers = { 'Authorization': `Bearer ${token}` }; // Add auth header for polling too
      const response = await fetch(`http://localhost:8080/api/games/${gameId}`, { headers });
      console.log('[DEBUG] Polled game state fetch response status:', response.status);
      
      if (!response.ok) {
        console.error('[ERROR] Failed to fetch polled game state:', {
          status: response.status,
          statusText: response.statusText,
          url: response.url
        });
        if (response.status === 404) {
          console.warn('[WARN] Game not found during polling, navigating to lobby.');
          navigate('/lobby');
        }
        return; 
      }

      const data = await response.json();
      console.log('[DEBUG] Received polled game state:', data);
      
      if (data.game) {
        console.log(`[DEBUG] Polled game data - gameId: ${data.game.id}, inProgress: ${data.game.inProgress}`);
        setGameData(data.game);
        const fetchedPlayers = data.game.players || [];
        const currentUserEmail = auth.currentUser?.email;
        setPlayers(processPlayersData(fetchedPlayers, currentUserEmail));
      } else {
        console.warn('[WARN] Polled data missing game object');
      }
      
      if (data.boardState) {
        console.log('[DEBUG] Received boardState in poll, updating local boardState.');
        setBoardState(data.boardState);
      } else if (data.game && !data.game.inProgress) {
        console.log('[DEBUG] Game not in progress (from poll) and no boardState in poll, clearing local boardState.');
        setBoardState(null);
      }

      // update whose the current turn
      if (data.game && data.game.currentTurn) {
        console.log('[DEBUG] Setting current turn from poll:', data.game.currentTurn);
        setCurrentTurn(data.game.currentTurn);
      }
    } catch (error) {
      console.error('[ERROR] Error fetching polled game state:', error);
  
    }
  };

  const handleTurnComplete = () => {
    if (!game || currentTurn === null) return;

    // get current player index
    const currentPlayerIndex = game.playerList.indexOf(currentTurn);
    let nextPlayerIndex;

    if (isSetupPhase) {
      // during setup phase:
      // first round: 0 -> 1 -> 2 -> 3
      // second round: 3 -> 2 -> 1 -> 0
      const isFirstRound = game.playerList.every((playerId: number) => {
        const player = players.find(p => p.id === playerId);
        return player?.devCards.roadBuilding === 0;
      });

      if (isFirstRound) {
        // move forward
        nextPlayerIndex = (currentPlayerIndex + 1) % game.playerList.length;
        if (nextPlayerIndex === 0) {
          // start second round
          nextPlayerIndex = game.playerList.length - 1;
        }
      } else {
        // move backward
        nextPlayerIndex = currentPlayerIndex - 1;
        if (nextPlayerIndex < 0) {
          // setup phase complete
          setIsSetupPhase(false);
          nextPlayerIndex = 0; // start with first player for regular game
        }
      }
    } else {
      // regular game: clockwise rotation
      nextPlayerIndex = (currentPlayerIndex + 1) % game.playerList.length;
    }

    setCurrentTurn(game.playerList[nextPlayerIndex]);
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

      // +++ DEBUG LOG: Check received vertices before setting state +++
      if (data.boardState && data.boardState.vertices) {
          const receivedVertices = data.boardState.vertices;
          console.log(`[GameRoom DEBUG] Received ${receivedVertices.length} vertices from API.`);
          if (receivedVertices.length > 0) {
              console.log('[GameRoom DEBUG] First received vertex:', JSON.stringify(receivedVertices[0]));
              console.log('[GameRoom DEBUG] Last received vertex:', JSON.stringify(receivedVertices[receivedVertices.length - 1]));
              // Check specifically for vertex 55
              const vertex55 = receivedVertices.find((v: any) => v.id === 55);
              console.log('[GameRoom DEBUG] Found vertex with ID 55 in received data:', !!vertex55, vertex55 ? JSON.stringify(vertex55) : 'Not Found');
          }
      } else {
          console.warn('[GameRoom DEBUG] No boardState.vertices received in API response.');
      }
      // +++ END DEBUG LOG +++

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
        
        // if current user is first player, enable their turn
        const currentPlayer = players.find(p => p.isCurrentUser);
        if (currentPlayer && currentPlayer.id === data.game.playerList[0]) {
          console.log('Enabling turn for current player:', currentPlayer.id);
          setIsCurrentTurn(true);
        }
      } else {
        console.warn('No player list in game data:', data.game);
      }

      // show a message to indicate game has started
      alert('Game has started! First player can now take their turn.');
    } catch (error: any) {
      console.error('Error in handleStartGame:', {
        error,
        message: error.message,
        stack: error.stack,
        gameId,
        currentGameState: game,
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
    if (!gameId || !selectedAction || !game) return;

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
      // Update player resources and game state
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
    if (!gameId || !game) return;

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
      setGame(data.game);
      
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
    if (!gameId || !game) return;

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
    if (!gameId || !game) return;

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
        setGame(data.game);
      }
    } catch (error) {
      console.error('Error playing development card:', error);
    }
  };

  // what is going on here???
  const handleSetupActionSuccess = (responseData: any) => {
    console.log('[GameRoom] Setup action successful, updating state:', responseData);
    if (responseData.game) {
      setGameData(responseData.game);
      // potentially update players list if responseData.game.players is more current
      const fetchedPlayers = responseData.game.players || [];
      const currentUserEmail = auth.currentUser?.email;
      setPlayers(processPlayersData(fetchedPlayers, currentUserEmail));
    }
    if (responseData.boardState) {
      setBoardState(responseData.boardState);
    }
    // potentially update currentTurn, isSetupPhase etc. based on new gameData
    if (responseData.game && responseData.game.currentTurn) {
        setCurrentTurn(responseData.game.currentTurn);
    }
    // If setup phase is completed based on game logic from backend, update isSetupPhase
    // Example: if (responseData.game.setupPhaseCompleted) { setIsSetupPhase(false); }
  };

  const handleQuitGame = () => {
    // potentially update game state to remove player from player list, for turn management
    // check if the player is in player list, if not, skip their turn
    // if only one player left, crown them the winner automatically. 
    navigate('/lobby');
  };

  const PlayerCard = ({ player, isHost }: { player: Player; isHost: boolean }) => {
    // Host is the first player in the player list
    // const isHost = game?.playerList[0] === player.id; // Old logic, gameData is better
    // const isHost = gameData?.playerList[0] === player.id; // Removed: isHost is now a prop

    return (
    <div 
      className={cn(
        "relative rounded-xl p-4 transition-all duration-200",
        "bg-white/20 backdrop-blur-sm",
        "border-[1.5px] shadow-lg hover:shadow-xl",
        gameData?.inProgress && currentTurn === player.id && "ring-2 ring-offset-2 ring-offset-blue-100/50" // Only ring if game in progress and current turn
      )}
      style={{
        borderColor: player.color
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
              backgroundColor: player.isCurrentUser ? 'white' : player.color,
              borderColor: player.color
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

      <div className="grid grid-cols-3 gap-1 text-xs text-gray-600 mb-2">
        <div className="flex items-center gap-1">
          <span>🎲</span>
            <span>{player.devCards.victoryPoint}</span>
        </div>
        <div className="flex items-center gap-1">
          <span>⚔️</span>
            <span>{player.devCards.knight}</span>
        </div>
        <div className="flex items-center gap-1">
          <span>🛣️</span>
            <span>{player.devCards.roadBuilding}</span>
          </div>
      </div>

      {player.isCurrentUser && player.resources && (
        <>
          <Separator className="my-2 bg-gray-200" />
          <div className="space-y-1">
            <div className="font-medium text-xs text-gray-700">Resources</div>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="flex items-center gap-1 text-gray-700">
                <span>🧱</span>
                <span>{player.resources.brick}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🌲</span>
                <span>{player.resources.wood}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>⛰️</span>
                <span>{player.resources.ore}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🌾</span>
                <span>{player.resources.wheat}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🐑</span>
                <span>{player.resources.sheep}</span>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
  };

  const ActionButtons = () => {
    const currentPlayer = players.find(p => p.isCurrentUser);
    const isCurrentPlayerTurn = currentPlayer && currentTurn === currentPlayer.id;
    
    if (!isCurrentPlayerTurn || isSetupPhase || !game?.inProgress) return null;

    return (
      <div className="fixed bottom-4 left-1/2 transform -translate-x-1/2 flex flex-col gap-4 items-center z-10">
        <div className="flex gap-4">
          <button
            onClick={() => handleActionClick('SETTLEMENT')}
            className={cn(
              "px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/20 backdrop-blur-sm border border-orange-200/50",
              selectedAction === 'SETTLEMENT' ? "ring-2 ring-orange-400" : "hover:bg-orange-50/50"
            )}
          >
            🏠 Settlement
          </button>
          <button
            onClick={() => handleActionClick('CITY')}
            className={cn(
              "px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/20 backdrop-blur-sm border border-blue-200/50",
              selectedAction === 'CITY' ? "ring-2 ring-blue-400" : "hover:bg-blue-50/50"
            )}
          >
            🏰 City
          </button>
          <button
            onClick={() => handleActionClick('ROAD')}
            className={cn(
              "px-4 py-2 rounded-xl font-medium shadow-lg transition-all",
              "bg-white/20 backdrop-blur-sm border border-brown-200/50",
              selectedAction === 'ROAD' ? "ring-2 ring-brown-400" : "hover:bg-brown-50/50"
            )}
          >
            🛣️ Road
          </button>
          <button
            onClick={() => handleActionClick('TRADE')}
            className="px-4 py-2 rounded-xl font-medium shadow-lg transition-all
                      bg-white/20 backdrop-blur-sm border border-green-200/50
                      hover:bg-green-50/50"
          >
            🤝 Trade
          </button>
          <button
            onClick={handleBuyDevCard}
            className="px-4 py-2 rounded-xl font-medium shadow-lg transition-all
                      bg-white/20 backdrop-blur-sm border border-yellow-200/50
                      hover:bg-yellow-50/50"
          >
            🎲 Buy Dev Card
          </button>
          <button
            onClick={handleEndTurn}
            className="px-4 py-2 rounded-xl font-medium shadow-lg transition-all
                      bg-white/20 backdrop-blur-sm border border-purple-200/50
                      hover:bg-purple-50/50"
          >
            ⏭️ End Turn
          </button>
        </div>

        {/* Development Cards */}
        {(devCards.knight > 0 || devCards.yearOfPlenty > 0 || devCards.monopoly > 0 || 
          devCards.roadBuilding > 0 || devCards.victoryPoint > 0) && (
          <div className="flex gap-2 mt-2">
            {devCards.knight > 0 && (
              <button
                onClick={() => handlePlayDevCard('knight')}
                className="px-3 py-1 rounded-lg text-sm font-medium bg-white/20 backdrop-blur-sm
                          border border-red-200/50 hover:bg-red-50/50 transition-all"
              >
                ⚔️ Knight ({devCards.knight})
              </button>
            )}
            {devCards.yearOfPlenty > 0 && (
              <button
                onClick={() => handlePlayDevCard('yearOfPlenty')}
                className="px-3 py-1 rounded-lg text-sm font-medium bg-white/20 backdrop-blur-sm
                          border border-green-200/50 hover:bg-green-50/50 transition-all"
              >
                📦 Year of Plenty ({devCards.yearOfPlenty})
              </button>
            )}
            {devCards.monopoly > 0 && (
              <button
                onClick={() => handlePlayDevCard('monopoly')}
                className="px-3 py-1 rounded-lg text-sm font-medium bg-white/20 backdrop-blur-sm
                          border border-blue-200/50 hover:bg-blue-50/50 transition-all"
              >
                💰 Monopoly ({devCards.monopoly})
              </button>
            )}
            {devCards.roadBuilding > 0 && (
              <button
                onClick={() => handlePlayDevCard('roadBuilding')}
                className="px-3 py-1 rounded-lg text-sm font-medium bg-white/20 backdrop-blur-sm
                          border border-brown-200/50 hover:bg-brown-50/50 transition-all"
              >
                🛣️ Road Building ({devCards.roadBuilding})
              </button>
            )}
            {devCards.victoryPoint > 0 && (
              <button
                className="px-3 py-1 rounded-lg text-sm font-medium bg-white/20 backdrop-blur-sm
                          border border-yellow-200/50"
              >
                👑 Victory Point ({devCards.victoryPoint})
              </button>
            )}
          </div>
        )}
      </div>
    );
  };

  const isCurrentUserHost = gameData && gameData.playerList && gameData.playerList.length > 0 && players.find(p => p.isCurrentUser)?.id === gameData.playerList[0];
  const showStartGameButton = gameData && !gameData.inProgress && !gameData.isGameOver && isCurrentUserHost;

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex overflow-hidden">
      <button
        onClick={handleQuitGame}
        className="fixed top-4 left-4 z-10 flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-red-600 text-sm font-medium rounded-xl hover:bg-red-50/50 transition-all shadow-lg hover:shadow-xl border border-red-200/50"
      >
        <XCircle size={16} />
        <span>Quit Game</span>
      </button>

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
                isSetupPhase={isSetupPhase}
                isCurrentTurn={currentTurn === players.find(p => p.isCurrentUser)?.id}
                onPlacementComplete={handleTurnComplete}
                selectedAction={selectedAction}
                onActionSelect={handleGameAction}
                boardState={boardState}
                gameConfig={gameData}
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
        <div className="space-y-4">
          {players.map(p => {
            const isHostForThisCard = gameData && gameData.playerList && gameData.playerList.length > 0 && gameData.playerList[0] === p.id;
            return <PlayerCard key={p.id} player={p} isHost={isHostForThisCard} />;
          })}
        </div>
      </div>
    </div>
  );
};

export default GameRoom; 