import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { getAuth, signOut } from 'firebase/auth';
import { XCircle, Gamepad2, Settings } from 'lucide-react';

interface Account {
  id: number;
  username: string;
  email: string;
  totalGames: number;
  totalWins: number;
  totalLosses: number;
  elo: number;
}

const HomePage = () => {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const auth = getAuth();
  const [account, setAccount] = useState<Account | null>(null);

  useEffect(() => {
    const loadUserData = async () => {
      if (!currentUser) return;

      try {
        const response = await fetch('http://localhost:8080/api/account/by-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await currentUser.getIdToken()}`
          },
          body: JSON.stringify({ email: currentUser.email })
        });

        if (!response.ok) throw new Error('Failed to fetch account');

        const userAccount = await response.json();
        setAccount(userAccount);
      } catch (error) {
        console.error('Error fetching user account:', error);
      }
    };

    loadUserData();
  }, [currentUser]);

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/signin');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex flex-col overflow-hidden">
      {/* Header */}
      <div className="w-full bg-white/10 backdrop-blur-sm border-b border-white/20">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-800/90">CooperCatan</h1>
          <div className="flex items-center gap-4">
            {currentUser ? (
              <>
                {account && (
                  <div className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/20">
                    <span className="text-gray-700/90 font-medium">
                      {account.username}
                    </span>
                    <span className="text-sm text-gray-600/90">ELO {account.elo}</span>
                  </div>
                )}
                <button
                  onClick={() => navigate('/lobby')}
                  className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                >
                  <Gamepad2 size={16} />
                  <span>Game Lobby</span>
                </button>
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
              </>
            ) : (
              <div className="flex items-center gap-4">
                <button
                  onClick={() => navigate('/signin')}
                  className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                >
                  Sign In
                </button>
                <button
                  onClick={() => navigate('/signup')}
                  className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                >
                  Sign Up
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto px-6 py-12">
          <div className="text-center space-y-8">
            <h2 className="text-4xl sm:text-5xl md:text-6xl font-bold text-gray-800/90">
              Build Your Empire in
              <span className="block mt-2 text-gray-700/90">Cooper Union's Catan</span>
            </h2>
            <p className="text-lg sm:text-xl text-gray-600/90 max-w-2xl mx-auto">
              Trade, build, and compete with players around Cooper Union in this classic board game.
            </p>
            {!currentUser && (
              <div className="flex justify-center gap-4">
                <button
                  onClick={() => navigate('/signup')}
                  className="px-8 py-3 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                >
                  Start Playing Free
                </button>
              </div>
            )}
          </div>

          {/* Game Features */}
          <div className="mt-24 grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-semibold text-gray-800/90 mb-2">Real-time Gameplay</h3>
              <p className="text-gray-600/90">
                Experience real-time gameplay with fellow Cooper Union students.
              </p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-semibold text-gray-800/90 mb-2">Competitive Rankings</h3>
              <p className="text-gray-600/90">
                Climb the leaderboard and prove your strategic prowess among your peers.
              </p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-semibold text-gray-800/90 mb-2">Custom Game Rooms</h3>
              <p className="text-gray-600/90">
                Create and join custom game rooms to play with your friends.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage; 