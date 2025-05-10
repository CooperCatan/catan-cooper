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
  hexes: any[];
  vertices: any[];
  edges: any[];
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
  const [game, setGame] = useState<{
    id: number;
    playerList: number[];
    winnerId: number | null;
    isGameOver: boolean;
    inProgress: boolean;
    gameName: string;
  } | null>(null);
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
  const [boardState, setBoardState] = useState<BoardState>({
    hexes: [],
    vertices: [],
    edges: []
  });

  useEffect(() => {
    const currentUser = auth.currentUser;
    if (!currentUser) {
      navigate('/signin');
      return;
    }

    // Fetch game data and players
    const fetchGameAndPlayers = async () => {
      try {
        // Fetch game data
        const gameResponse = await fetch(`http://localhost:8080/api/games/${gameId}`);
        if (!gameResponse.ok) {
          throw new Error('Failed to fetch game');
        }
        const gameData = await gameResponse.json();
        setGame(gameData);

        // Fetch all accounts to get player details
        const accountsResponse = await fetch('http://localhost:8080/api/account');
        if (!accountsResponse.ok) {
          throw new Error('Failed to fetch accounts');
        }
        const accounts = await accountsResponse.json();

        // Map player IDs to full account details
        const playerDetails = gameData.playerList.map((playerId: number, index: number) => {
          const account = accounts.find((acc: any) => acc.id === playerId);
          if (!account) return null;

          return {
            id: account.id,
            username: account.username,
            isCurrentUser: account.email === currentUser.email,
            devCards: {
              knight: 0,
              yearOfPlenty: 0,
              monopoly: 0,
              roadBuilding: 0,
              victoryPoint: 0
            },
            color: PLAYER_COLORS[index + 1],
            ...(account.email === currentUser.email && {
              resources: {
                brick: 0,
                wood: 0,
                ore: 0,
                wheat: 0,
                wool: 0
              }
            })
          };
        }).filter((player: Player | null): player is Player => player !== null);

        setPlayers(playerDetails);

        // Set current turn to the current user's ID if they're in the game
        const currentPlayer = playerDetails.find((p: Player) => p.isCurrentUser);
        if (currentPlayer) {
          setCurrentTurn(currentPlayer.id);
        }
      } catch (error) {
        console.error('Error fetching game data:', error);
      }
    };

    fetchGameAndPlayers();

    // Set up countdown timer
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

  const handleTurnComplete = () => {
    if (!game || currentTurn === null) return;

    // Get current player index
    const currentPlayerIndex = game.playerList.indexOf(currentTurn);
    let nextPlayerIndex;

    if (isSetupPhase) {
      // During setup phase:
      // First round: 0 -> 1 -> 2 -> 3
      // Second round: 3 -> 2 -> 1 -> 0
      const isFirstRound = game.playerList.every(playerId => {
        const player = players.find(p => p.id === playerId);
        return player?.devCards.roadBuilding === 0;
      });

      if (isFirstRound) {
        // Move forward
        nextPlayerIndex = (currentPlayerIndex + 1) % game.playerList.length;
        if (nextPlayerIndex === 0) {
          // Start second round
          nextPlayerIndex = game.playerList.length - 1;
        }
      } else {
        // Move backward
        nextPlayerIndex = currentPlayerIndex - 1;
        if (nextPlayerIndex < 0) {
          // Setup phase complete
          setIsSetupPhase(false);
          nextPlayerIndex = 0; // Start with first player for regular game
        }
      }
    } else {
      // Regular game: clockwise rotation
      nextPlayerIndex = (currentPlayerIndex + 1) % game.playerList.length;
    }

    setCurrentTurn(game.playerList[nextPlayerIndex]);
  };

  const handleStartGame = async () => {
    console.log('handleStartGame called, gameId:', gameId);
    if (!gameId) {
      console.log('No gameId found, returning');
      return;
    }

    try {
      console.log('Making start game request to:', `http://localhost:8080/api/games/${gameId}/start`);
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/start`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('Start game response status:', response.status);
      console.log('Start game response headers:', Object.fromEntries(response.headers.entries()));
      
      // Always try to read the response body, regardless of status
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
      console.log('Start game successful, processing response data:', data);

      // Validate response data
      if (!data.game) {
        console.error('Invalid response data - missing game object:', data);
        throw new Error('Invalid server response: missing game data');
      }

      // Update game state
      console.log('Updating game state with:', data.game);
      setGame(data.game);
      
      // Update board state
      if (data.boardState) {
        console.log('Updating board state with:', data.boardState);
        const { hexes, vertices, edges } = data.boardState;
        setBoardState({
          hexes: hexes || [],
          vertices: vertices || [],
          edges: edges || []
        });
      } else {
        console.warn('No board state received in response');
      }

      // Set initial turn order - host (first player) goes first
      if (data.game.playerList && data.game.playerList.length > 0) {
        console.log('Setting initial turn to:', data.game.playerList[0]);
        setCurrentTurn(data.game.playerList[0]);
        
        // If current user is first player, enable their turn
        const currentPlayer = players.find(p => p.isCurrentUser);
        if (currentPlayer && currentPlayer.id === data.game.playerList[0]) {
          console.log('Enabling turn for current player:', currentPlayer.id);
          setIsCurrentTurn(true);
        }
      } else {
        console.warn('No player list in game data:', data.game);
      }

      // Show a message to indicate game has started
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

  const handleQuitGame = async () => {
    if (!gameId || !auth.currentUser) return;

    try {
      // Find the current user's account ID from the players list
      const currentPlayer = players.find(p => p.isCurrentUser);
      if (!currentPlayer) {
        console.error('Current player not found in game');
        navigate('/lobby');
        return;
      }

      // Call the leave game API
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/leave`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ accountId: currentPlayer.id })
      });

      if (!response.ok) {
        throw new Error('Failed to leave game');
      }

      // Navigate back to lobby
      navigate('/lobby');
    } catch (error) {
      console.error('Error leaving game:', error);
      // Still navigate to lobby even if there's an error
      navigate('/lobby');
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
      
      // Update game state
      setGame(data.game);
      
      // If game is over, show winner
      if (data.game.isGameOver) {
        const winner = players.find(p => p.id === data.game.winnerId);
        alert(`Game Over! ${winner?.username} has won the game!`);
        navigate('/lobby');
        return;
      }

      // Move to next player's turn
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
          // For Knight card, we need to select a hex and a player to steal from
          const targetHex = window.prompt('Enter the hex number to move the robber to:');
          const targetPlayer = window.prompt('Enter the player ID to steal from:');
          if (!targetHex || !targetPlayer) return;
          additionalParams = {
            targetHex: parseInt(targetHex),
            targetPlayerId: parseInt(targetPlayer)
          };
          break;

        case 'yearOfPlenty':
          // For Year of Plenty, we need to select two resources
          const resource1 = window.prompt('Select first resource (brick, wood, ore, wheat, wool):');
          const resource2 = window.prompt('Select second resource (brick, wood, ore, wheat, wool):');
          if (!resource1 || !resource2) return;
          additionalParams = { resource1, resource2 };
          break;

        case 'monopoly':
          // For Monopoly, we need to select one resource type
          const resource = window.prompt('Select resource to monopolize (brick, wood, ore, wheat, wool):');
          if (!resource) return;
          additionalParams = { resource };
          break;

        case 'roadBuilding':
          // For Road Building, we need to select two edges
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
      // Update dev cards count
      setDevCards(prev => ({
        ...prev,
        [cardType]: prev[cardType as keyof typeof prev] - 1
      }));
      
      // Update game state if needed
      if (data.game) {
        setGame(data.game);
      }
    } catch (error) {
      console.error('Error playing development card:', error);
    }
  };

  const PlayerCard = ({ player }: { player: Player }) => {
    // Host is the first player in the player list
    const isHost = game?.playerList[0] === player.id;

    return (
    <div 
      className={cn(
        "relative rounded-xl p-4 transition-all duration-200",
        "bg-white/20 backdrop-blur-sm",
        "border-[1.5px] shadow-lg hover:shadow-xl",
        currentTurn === player.id && "ring-2 ring-offset-2 ring-offset-blue-100/50"
      )}
      style={{
        borderColor: player.color
      }}
    >
        {game?.inProgress ? (
          currentTurn === player.id && (
        <div className="absolute -top-2 -right-2 px-2 py-0.5 bg-green-500/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
          Current Turn
            </div>
          )
        ) : (
          <div className="absolute -top-2 -right-2 px-2 py-0.5 bg-green-500/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
            Waiting to Start
          </div>
        )}
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
                <span>{player.resources.wool}</span>
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
            <GameBoard 
              gameId={Number(gameId)}
              accountId={Number(auth.currentUser?.uid)}
              isSetupPhase={isSetupPhase}
              isCurrentTurn={currentTurn === players.find(p => p.isCurrentUser)?.id}
              onPlacementComplete={handleTurnComplete}
              selectedAction={selectedAction}
              onActionSelect={handleGameAction}
            />
          </div>
        </div>
      </div>
      <ActionButtons />

      <div className="w-96 bg-white/10 backdrop-blur-sm border-l border-white/20 p-6 overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800/90">Players</h2>
          {game && !game.inProgress && !game.isGameOver && game.playerList[0] === players.find(p => p.isCurrentUser)?.id && (
            <button
              onClick={handleStartGame}
              className="px-4 py-2 bg-green-100/50 text-green-700/90 hover:bg-green-100/60 font-medium rounded-xl transition-all shadow-lg hover:shadow-xl border border-green-200/50 backdrop-blur-sm"
            >
              Start Game
            </button>
          )}
        </div>
        <div className="space-y-4">
          {players.map(player => (
            <PlayerCard key={player.id} player={player} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default GameRoom; 