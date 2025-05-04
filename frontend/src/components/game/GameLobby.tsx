import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signOut } from 'firebase/auth';

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

const GameLobby = () => {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [account, setAccount] = useState<Account | null>(null);
  const navigate = useNavigate();
  const auth = getAuth();

  useEffect(() => {
    const loadUserData = async () => {
      const user = auth.currentUser;
      if (!user) {
        console.log('No user found, redirecting to signin');
        navigate('/signin');
        return;
      }

      console.log('Current user email:', user.email);

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
        console.log('Fetched accounts:', accounts);
        
        const userAccount = accounts.find(acc => acc.email === user.email);
        console.log('Found user account:', userAccount);
        
        if (userAccount) {
          setAccount(userAccount);
        } else {
          console.log('No matching account found for email:', user.email);
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
  }, [auth, navigate]);

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
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-catan-brick">Game Lobby</h1>
          <div className="flex items-center space-x-4">
            {account && (
              <span className="text-gray-600">
                Welcome, <span className="font-medium">{account.username}</span>
              </span>
            )}
            <button
              onClick={handleSignOut}
              className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
            >
              Sign Out
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto p-8">
        <div className="mb-8">
          <button
            onClick={handleCreateGame}
            className="px-6 py-3 bg-catan-brick text-white rounded-lg hover:bg-catan-brick/90 transition-colors"
          >
            Create New Game
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {games.map((game) => (
            <div
              key={game.gameId}
              className="bg-white rounded-xl shadow-md p-6 space-y-4"
            >
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">Game #{game.gameId}</h3>
                <span className={`px-3 py-1 rounded-full text-sm ${
                  game.status === 'waiting' ? 'bg-green-100 text-green-800' :
                  game.status === 'in_progress' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {game.status.replace('_', ' ')}
                </span>
              </div>

              <div>
                <h4 className="text-sm font-medium text-gray-500">Players</h4>
                <ul className="mt-1 space-y-1">
                  {game.players.map((player, index) => (
                    <li key={index} className="text-sm text-gray-700">{player}</li>
                  ))}
                </ul>
              </div>

              {game.winner && (
                <div className="text-sm text-gray-600">
                  Winner: <span className="font-medium">{game.winner}</span>
                </div>
              )}

              {game.status === 'waiting' && (
                <button
                  onClick={() => handleJoinGame(game.gameId)}
                  className="w-full px-4 py-2 bg-catan-brick text-white rounded-lg hover:bg-catan-brick/90 transition-colors"
                >
                  Join Game
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default GameLobby; 