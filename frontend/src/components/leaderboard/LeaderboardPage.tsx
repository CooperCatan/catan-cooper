import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { getAuth, signOut } from 'firebase/auth';
import { XCircle, Home, Settings, Trophy } from 'lucide-react';

interface Account {
  id: number;
  username: string;
  email: string;
  totalGames: number;
  totalWins: number;
  totalLosses: number;
  elo: number;
}

const LeaderboardPage = () => {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const auth = getAuth();
  const [account, setAccount] = useState<Account | null>(null);
  const [players, setPlayers] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      if (!currentUser) {
        navigate('/signin');
        return;
      }

      try {
        // Fetch current user's account
        const accountResponse = await fetch('http://localhost:8080/api/account/by-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await currentUser.getIdToken()}`
          },
          body: JSON.stringify({ email: currentUser.email })
        });

        if (accountResponse.ok) {
          const accountData = await accountResponse.json();
          setAccount(accountData);
        }

        // Fetch all players for leaderboard
        const playersResponse = await fetch('http://localhost:8080/api/accounts', {
          headers: {
            'Authorization': `Bearer ${await currentUser.getIdToken()}`
          }
        });
        if (playersResponse.ok) {
          const playersData = await playersResponse.json();
          setPlayers(playersData);
        }
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [currentUser, navigate]);

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/signin');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  if (loading) {
    return (
      <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center">
        <div className="animate-pulse text-gray-600">Loading leaderboard...</div>
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
              <span className="text-2xl font-bold text-gray-800/90">Leaderboard</span>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            {account && (
              <div className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/20">
                <span className="text-gray-700/90 font-medium">
                  {account.username}
                </span>
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
        <div className="max-w-4xl mx-auto">
          <div className="bg-white/20 backdrop-blur-sm rounded-xl border border-white/20 shadow-lg overflow-hidden">
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <Trophy className="w-6 h-6 text-yellow-500" />
                <h2 className="text-xl font-bold text-gray-800/90">Player Rankings</h2>
              </div>
              <div className="space-y-4">
                {players.map((player, index) => (
                  <div
                    key={player.id}
                    className={`flex items-center gap-4 p-4 bg-white/10 rounded-xl border border-white/10 transition-all ${
                      account && player.id === account.id ? 'ring-2 ring-blue-400/50' : ''
                    }`}
                  >
                    <div className="flex items-center justify-center w-8 h-8 rounded-full bg-white/20 text-gray-700/90 font-bold">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-gray-800/90">{player.username}</span>
                        {index === 0 && (
                          <span className="px-2 py-0.5 bg-yellow-100/50 text-yellow-700/90 text-xs font-medium rounded-full">
                            Top Player
                          </span>
                        )}
                      </div>
                      <div className="text-sm text-gray-600/90">
                        Games: {player.totalGames} | Wins: {player.totalWins} | Win Rate: {
                          ((player.totalWins / (player.totalGames || 1)) * 100).toFixed(1)
                        }%
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-lg font-bold text-gray-800/90">{player.elo}</div>
                      <div className="text-xs text-gray-600/90">ELO Rating</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LeaderboardPage; 