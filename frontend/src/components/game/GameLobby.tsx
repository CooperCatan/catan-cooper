import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signOut } from 'firebase/auth';
import { PLAYER_COLORS } from './GameBoard';
import { XCircle, Home, Settings } from 'lucide-react';
import { useAuth } from '../auth/AuthProvider';

interface Game {
  gameId: number;
  players: string[];
  status: 'waiting' | 'in_progress' | 'completed';
  winner?: string;
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

const cn = (...classes: string[]) => classes.filter(Boolean).join(' ');

const GameLobby = () => {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [account, setAccount] = useState<Account | null>(null);
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const auth = getAuth();

  useEffect(() => {
    const loadUserData = async () => {
      if (!currentUser) {
        return;
      }

      try {
        const response = await fetch('http://localhost:8080/api/accounts', {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error('Failed to fetch accounts:', errorText);
          throw new Error('Failed to fetch accounts');
        }

        const accounts: Account[] = await response.json();
        const userAccount = accounts.find(acc => acc.email === currentUser.email);
        
        if (userAccount) {
          setAccount(userAccount);
        }
      } catch (error) {
        console.error('Error fetching user account:', error);
      }
    };

    loadUserData();
    
    const syntheticGames: Game[] = [
      {
        gameId: 1,
        players: ['Player1', 'Player2', 'Player3'],
        status: 'in_progress'
      },
      {
        gameId: 2,
        players: ['Player4', 'Player5'],
        status: 'waiting'
      },
      {
        gameId: 3,
        players: ['Player6', 'Player7', 'Player8', 'Player9'],
        status: 'completed',
        winner: 'Player7'
      }
    ];
    setGames(syntheticGames);
    setLoading(false);
  }, [currentUser]);

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  const handleJoinGame = (gameId: number) => {
    // nav to gameroom with gameId 
    navigate(`/game/${gameId}`);
  };

  const handleCreateGame = () => {
    // TODO: Implement shuffled game board creation logic so each lobby is unique 
    console.log('Creating new game');
  };

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
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
            >
              <Home size={16} />
              <span>Home</span>
            </button>
            <h1 className="text-2xl font-bold text-gray-800/90">Game Lobby</h1>
          </div>
          <div className="flex items-center gap-4">
            {account && (
              <div className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/20">
                <span className="text-gray-700/90 font-medium">{account.username}</span>
                <span className="text-sm text-gray-600/90">ELO {account.elo}</span>
              </div>
            )}
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
            onClick={handleCreateGame}
            className="px-6 py-3 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
          >
            Create New Game
          </button>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {games.map((game) => (
              <div
                key={game.gameId}
                className="relative bg-white/20 backdrop-blur-sm rounded-xl p-6 space-y-4 border border-white/20 shadow-lg hover:shadow-xl transition-all"
              >
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-xl font-bold text-gray-800/90">Game #{game.gameId}</h3>
                  <div className={cn(
                    "px-3 py-1 text-sm font-medium rounded-full backdrop-blur-sm border",
                    game.status === 'waiting' ? "bg-green-100/50 text-green-700/90 border-green-200/50" :
                    game.status === 'in_progress' ? "bg-yellow-100/50 text-yellow-700/90 border-yellow-200/50" :
                    "bg-gray-100/50 text-gray-700/90 border-gray-200/50"
                  )}>
                    {game.status === 'waiting' ? 'Waiting' :
                     game.status === 'in_progress' ? 'In Progress' :
                     'Completed'}
                  </div>
                </div>

                <div className="space-y-2">
                  <h4 className="text-sm font-medium text-gray-600/90">Players</h4>
                  <div className="space-y-2">
                    {game.players.map((player, index) => (
                      <div 
                        key={index}
                        className="flex items-center gap-2 px-3 py-2 bg-white/10 rounded-lg border border-white/10"
                      >
                        <div 
                          className="w-3 h-3 rounded-full"
                          style={{ backgroundColor: PLAYER_COLORS[index + 1] }}
                        />
                        <span className="text-sm text-gray-700/90">{player}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {game.winner && (
                  <div className="flex items-center gap-2 mt-4">
                    <span className="text-sm text-gray-600/90">Winner:</span>
                    <span className="text-sm font-medium text-gray-800/90">{game.winner}</span>
                  </div>
                )}

                {game.status === 'waiting' && (
                  <button
                    onClick={() => handleJoinGame(game.gameId)}
                    className="w-full mt-4 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                  >
                    Join Game
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default GameLobby; 