import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signOut, User } from 'firebase/auth';
import { PLAYER_COLORS } from './GameBoard';
import { XCircle, Home, Settings, X } from 'lucide-react';
import { useAuth } from '../auth/AuthProvider';

interface Game {
  gameId: number;
  playerList: number[];
  winnerId: number | null;
  isGameOver: boolean;
  inProgress: boolean;
  createdAt: string;
  gameName: string;
  players?: Account[];
}

interface Account {
  id: number;
  username: string;
  email: string;
  totalGames: number;
  totalWins: number;
  totalLosses: number;
  elo: number;
}

interface AuthContextType {
  currentUser: User | null;
}

const cn = (...classes: (string | { [key: string]: boolean })[]) => {
  return classes
    .map(cls => 
      typeof cls === 'string' 
        ? cls 
        : Object.entries(cls)
            .filter(([_, value]) => value)
            .map(([key]) => key)
            .join(' ')
    )
    .filter(Boolean)
    .join(' ');
};

const GameCard: React.FC<{ 
  game: Game; 
  account: Account | null; 
  onJoin: (gameId: number) => void;
  onLeave: (gameId: number) => void;
}> = ({
  game,
  account,
  onJoin,
  onLeave
}) => {
  const navigate = useNavigate();
  const isCompleted = game.isGameOver || game.winnerId !== null;
  const isUserInGame = account && game.players?.some(p => p.id === account.id);
  const canJoin = !isCompleted && 
                  !game.inProgress &&
                  game.players && 
                  game.players.length < 4 && 
                  account &&
                  !isUserInGame;

  const handleLeaveGame = async () => {
    if (!account) return;
    onLeave(game.gameId);
  };

  const getButtonText = () => {
    if (isCompleted) return 'Game Over';
    if (isUserInGame) return 'Go to Board';
    return 'Join Game';
  };

  const handleButtonClick = () => {
    if (canJoin || isUserInGame) {
      onJoin(game.gameId);
    }
  };

  const handleGoToBoard = () => {
    navigate(`/game/${game.gameId}`);
  };

  return (
    <div className={cn(
      "relative bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20 shadow-lg hover:shadow-xl transition-all flex flex-col min-h-[320px]",
      isCompleted ? "opacity-75" : ""
    )}>
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="space-y-1">
          <h3 className={cn(
            "text-xl font-bold",
            isCompleted ? "text-gray-600/90" : "text-gray-800/90"
          )}>Game #{game.gameId}</h3>
          <p className={cn(
            "text-sm",
            isCompleted ? "text-gray-400/90" : "text-gray-600/90"
          )}>{game.gameName}</p>
        </div>
        <div className={cn(
          "px-3 py-1 text-sm font-medium rounded-full backdrop-blur-sm border",
          isCompleted ? "bg-gray-100/20 text-gray-500/90 border-gray-200/20" :
          game.inProgress ? "bg-yellow-100/50 text-yellow-700/90 border-yellow-200/50" :
          "bg-green-100/50 text-green-700/90 border-green-200/50"
        )}>
          {isCompleted ? 'Completed' :
           game.inProgress ? 'In Progress' :
           'Waiting'}
        </div>
      </div>

      {/* Players Section */}
      <div className="flex-1">
        <h4 className="text-sm font-medium text-gray-600/90 mb-2">Players</h4>
        <div className="space-y-2 h-[160px]">
          {game.players?.map((player, index) => (
            <div 
              key={index}
              className="flex items-center gap-2 px-3 py-2 bg-white/10 rounded-lg border border-white/10"
            >
              <div 
                className="w-3 h-3 rounded-full"
                style={{ backgroundColor: PLAYER_COLORS[index + 1] }}
              />
              <div className="flex items-center gap-1">
                <span className="text-sm text-gray-700/90">{player.username}</span>
                <span className="text-xs text-gray-500">({player.elo})</span>
              </div>
              {game.winnerId === player.id && (
                <span className="ml-auto text-sm text-yellow-600 font-medium">Winner</span>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Join/Leave Button Section */}
      <div className="pt-4 border-t border-white/10">
        {isUserInGame && !isCompleted ? (
          <div className="flex gap-3">
            <button
              onClick={handleGoToBoard}
              className="flex-1 px-4 py-2 bg-blue-100/50 text-blue-700/90 hover:bg-blue-100/60 font-medium rounded-xl transition-all shadow-lg hover:shadow-xl border border-blue-200/50 backdrop-blur-sm"
            >
              Go to Board
            </button>
            <button
              onClick={handleLeaveGame}
              className="flex-1 px-4 py-2 bg-red-100/50 text-red-700/90 hover:bg-red-100/60 font-medium rounded-xl transition-all shadow-lg hover:shadow-xl border border-red-200/50 backdrop-blur-sm"
            >
              Leave Game
            </button>
          </div>
        ) : (
          <button
            onClick={handleButtonClick}
            disabled={!canJoin && !isUserInGame}
            className={cn(
              "w-full px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all shadow-lg border",
              isCompleted 
                ? "bg-gray-100/20 text-gray-500/90 cursor-not-allowed border-gray-200/20"
                : game.inProgress
                  ? "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
                  : canJoin 
                    ? "bg-white/20 text-gray-800/90 hover:bg-white/30 hover:shadow-xl border-white/20" 
                    : "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
            )}
          >
            {getButtonText()}
          </button>
        )}
      </div>
    </div>
  );
};

const GameLobby = () => {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newGameName, setNewGameName] = useState('');
  const [username, setUsername] = useState<string>('');
  const navigate = useNavigate();
  const { currentUser } = useAuth() as AuthContextType;
  const auth = getAuth();

  // Fetch username on component mount
  useEffect(() => {
    const fetchUsername = async () => {
      if (!currentUser) {
        setLoading(false);
        return;
      }
      
      try {
        const idToken = await currentUser.getIdToken();
        const response = await fetch('http://localhost:8080/api/account/username', {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${idToken}`,
            'Content-Type': 'application/json'
          }
        });

        if (!response.ok) {
          if (response.status === 404) {
            console.error('Username not found');
            setLoading(false);
            return;
          }
          throw new Error(`Failed to fetch username: ${response.status}`);
        }

        const data = await response.json();
        setUsername(data.username);
        setLoading(false);
      } catch (error) {
        console.error('Error fetching username:', error);
        setLoading(false);
      }
    };

    fetchUsername();
  }, [currentUser]);

  // Fetch games periodically
  useEffect(() => {
    const fetchGames = async () => {
      if (!currentUser) return;

      try {
        const idToken = await currentUser.getIdToken();
        const response = await fetch('http://localhost:8080/api/games', {
          headers: {
            'Authorization': `Bearer ${idToken}`
          }
        });

        if (!response.ok) throw new Error('Failed to fetch games');

        const gamesData = await response.json();
        setGames(gamesData);
        setLoading(false);
      } catch (error) {
        console.error('Error fetching games:', error);
      }
    };

    // Initial fetch
    fetchGames();

    // Set up polling every 5 seconds
    const interval = setInterval(fetchGames, 5000);

    return () => clearInterval(interval);
  }, [currentUser]);

  const handleJoinGame = async (gameId: number) => {
    if (!currentUser) return;

    try {
      const idToken = await currentUser.getIdToken();
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/players`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${idToken}`
        }
      });

      if (!response.ok) throw new Error('Failed to join game');

      navigate(`/game/${gameId}`);
    } catch (error) {
      console.error('Error joining game:', error);
    }
  };

  const handleLeaveGame = async (gameId: number) => {
    if (!currentUser) return;

    try {
      const idToken = await currentUser.getIdToken();
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/players`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${idToken}`
        }
      });

      if (!response.ok) throw new Error('Failed to leave game');

      // Refresh games list
      window.location.reload();
    } catch (error) {
      console.error('Error leaving game:', error);
    }
  };

  const handleCreateGame = async () => {
    if (!currentUser || !newGameName.trim()) return;

    try {
      const idToken = await currentUser.getIdToken();
      const response = await fetch('http://localhost:8080/api/games', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${idToken}`
        },
        body: JSON.stringify({
          gameName: newGameName.trim()
        })
      });

      if (!response.ok) {
        if (response.status === 429) {
          alert('Please wait a minute before creating another game');
          return;
        }
        const errorData = await response.text();
        throw new Error(`Failed to create game: ${errorData}`);
      }

      const newGame = await response.json();
      
      // Close modal and clear input
      setIsCreateModalOpen(false);
      setNewGameName('');

      // Refresh games list instead of full page reload
      const gamesResponse = await fetch('http://localhost:8080/api/games', {
        headers: {
          'Authorization': `Bearer ${idToken}`
        }
      });

      if (gamesResponse.ok) {
        const updatedGames = await gamesResponse.json();
        setGames(updatedGames);
      }
    } catch (error) {
      console.error('Error creating game:', error);
      alert('Failed to create game. Please try again.');
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  // Sort games with "Waiting" games first
  const sortedGames = [...games].sort((a, b) => {
    const aIsWaiting = !a.isGameOver && !a.winnerId && a.players && a.players.length < 4;
    const bIsWaiting = !b.isGameOver && !b.winnerId && b.players && b.players.length < 4;
    
    if (aIsWaiting && !bIsWaiting) return -1;
    if (!aIsWaiting && bIsWaiting) return 1;
    return b.gameId - a.gameId;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 p-8">
        <div className="max-w-6xl mx-auto">
          <p>Loading games...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex flex-col overflow-hidden">
      {/* Header */}
      <div className="w-full bg-white/10 backdrop-blur-sm border-b border-white/20">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
            >
              <Home size={16} />
              <span>Home</span>
            </button>
            <div className="flex items-center px-4 py-2">
              <span className="text-2xl font-bold text-gray-800/90">Game Lobby</span>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <div className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/20">
              <span className="text-gray-700/90 font-medium">{username}</span>
            </div>
            <button
              onClick={() => navigate('/settings')}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
            >
              <Settings size={16} />
              <span>Settings</span>
            </button>
            <button
              onClick={handleSignOut}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-red-600 text-sm font-medium rounded-xl hover:bg-red-50/50 transition-all shadow-lg hover:shadow-xl border border-red-200/50"
            >
              <XCircle size={16} />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-7xl mx-auto space-y-6">
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-6 py-3 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
          >
            Create New Game
          </button>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sortedGames.map((game) => (
              <GameCard
                key={`game-${game.gameId}`}
                game={game}
                account={currentUser ? { id: parseInt(currentUser.uid), username, email: currentUser.email!, totalGames: 0, totalWins: 0, totalLosses: 0, elo: 1000 } : null}
                onJoin={handleJoinGame}
                onLeave={handleLeaveGame}
              />
            ))}
          </div>
        </div>
      </div>

      {/* Create Game Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center p-6">
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 w-full max-w-md border border-white/20 shadow-lg hover:shadow-xl transition-all">
            {/* Header */}
            <div className="flex items-center justify-between mb-4">
              <div className="space-y-1">
                <h2 className="text-xl font-bold text-gray-800/90">Create New Game</h2>
              </div>
              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="text-gray-500/80 hover:text-gray-700/90 transition-colors"
              >
                <X size={20} />
              </button>
            </div>

            <div className="space-y-6">
              {/* Game Name Input */}
              <div className="space-y-2">
                <label htmlFor="gameName" className="text-sm font-medium text-gray-600/90">
                  Game Name
                </label>
                <input
                  type="text"
                  id="gameName"
                  value={newGameName}
                  onChange={(e) => setNewGameName(e.target.value)}
                  placeholder="Enter a name for your game"
                  className="w-full px-3 py-2 bg-white/10 backdrop-blur-sm border border-white/10 rounded-lg text-gray-700/90 placeholder:text-gray-500/50 focus:outline-none focus:ring-2 focus:ring-white/30"
                />
              </div>

              {/* Player Info Section */}
              <div className="flex-1">
                <h4 className="text-sm font-medium text-gray-600/90 mb-2">Players</h4>
                <div className="space-y-2">
                  <div className="flex items-center gap-2 px-3 py-2 bg-white/10 rounded-lg border border-white/10">
                    <div 
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: PLAYER_COLORS[1] }}
                    />
                    <div className="flex items-center gap-1">
                      <span className="text-sm text-gray-700/90">{username}</span>
                    </div>
                    <span className="ml-auto text-sm text-gray-600/90 font-medium">Host</span>
                  </div>
                </div>
              </div>

              {/* Buttons Section */}
              <div className="pt-4 border-t border-white/10 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsCreateModalOpen(false)}
                  className="px-4 py-2 bg-white/10 backdrop-blur-sm text-gray-700/90 font-medium rounded-xl hover:bg-white/20 transition-all border border-white/10"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateGame}
                  type="submit"
                  disabled={!newGameName.trim()}
                  className={cn(
                    "px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all border",
                    newGameName.trim()
                      ? "bg-white/20 text-gray-800/90 hover:bg-white/30 border-white/20" 
                      : "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
                  )}
                >
                  Create Game
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GameLobby; 