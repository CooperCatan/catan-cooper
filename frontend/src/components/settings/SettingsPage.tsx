import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, updatePassword, updateEmail } from 'firebase/auth';
import { Home, Save, ArrowLeft } from 'lucide-react';

interface Account {
  id: number;
  username: string;
  email: string;
  totalGames: number;
  totalWins: number;
  totalLosses: number;
  elo: number;
}

const SettingsPage = () => {
  const navigate = useNavigate();
  const auth = getAuth();
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

  // Form states
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  useEffect(() => {
    const loadUserData = async () => {
      const user = auth.currentUser;
      if (!user) {
        navigate('/signin');
        return;
      }

      try {
        const response = await fetch('http://localhost:8080/api/accounts', {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });

        if (!response.ok) throw new Error('Failed to fetch account');

        const accounts: Account[] = await response.json();
        const userAccount = accounts.find(acc => acc.email === user.email);
        
        if (userAccount) {
          setAccount(userAccount);
          setUsername(userAccount.username);
          setEmail(userAccount.email);
        }
      } catch (error) {
        console.error('Error fetching user account:', error);
        setMessage({ type: 'error', text: 'Failed to load account data' });
      } finally {
        setLoading(false);
      }
    };

    loadUserData();
  }, [auth, navigate]);

  const handleUpdateUsername = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!account) return;

    try {
      const response = await fetch(`http://localhost:8080/api/accounts/${account.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username })
      });

      if (!response.ok) throw new Error('Failed to update username');
      
      setMessage({ type: 'success', text: 'Username updated successfully' });
    } catch (error) {
      console.error('Error updating username:', error);
      setMessage({ type: 'error', text: 'Failed to update username' });
    }
  };

  const handleUpdateEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!auth.currentUser) return;

    try {
      await updateEmail(auth.currentUser, email);
      setMessage({ type: 'success', text: 'Email updated successfully' });
    } catch (error) {
      console.error('Error updating email:', error);
      setMessage({ type: 'error', text: 'Failed to update email' });
    }
  };

  const handleUpdatePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!auth.currentUser) return;

    if (newPassword !== confirmPassword) {
      setMessage({ type: 'error', text: 'Passwords do not match' });
      return;
    }

    try {
      await updatePassword(auth.currentUser, newPassword);
      setMessage({ type: 'success', text: 'Password updated successfully' });
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      console.error('Error updating password:', error);
      setMessage({ type: 'error', text: 'Failed to update password' });
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex flex-col overflow-hidden">
      {/* Header */}
      <div className="w-full bg-white/10 backdrop-blur-sm border-b border-white/20">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/lobby')}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
            >
              <ArrowLeft size={16} />
              <span>Back to Lobby</span>
            </button>
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-700/90 text-sm font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
            >
              <Home size={16} />
              <span>Home</span>
            </button>
            <h1 className="text-2xl font-bold text-gray-800/90">Settings</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-2xl mx-auto space-y-6">
          {message && (
            <div className={cn(
              "p-4 rounded-xl border backdrop-blur-sm",
              message.type === 'success' 
                ? "bg-green-100/50 border-green-200/50 text-green-700" 
                : "bg-red-100/50 border-red-200/50 text-red-700"
            )}>
              {message.text}
            </div>
          )}

          {/* Account Stats */}
          {account && (
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
              <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Account Statistics</h2>
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3 bg-white/10 rounded-lg border border-white/10">
                  <div className="text-sm text-gray-600/90">Total Games</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.totalGames}</div>
                </div>
                <div className="p-3 bg-white/10 rounded-lg border border-white/10">
                  <div className="text-sm text-gray-600/90">Win Rate</div>
                  <div className="text-lg font-medium text-gray-800/90">
                    {((account.totalWins / (account.totalGames || 1)) * 100).toFixed(1)}%
                  </div>
                </div>
                <div className="p-3 bg-white/10 rounded-lg border border-white/10">
                  <div className="text-sm text-gray-600/90">ELO Rating</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.elo}</div>
                </div>
                <div className="p-3 bg-white/10 rounded-lg border border-white/10">
                  <div className="text-sm text-gray-600/90">Total Wins</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.totalWins}</div>
                </div>
              </div>
            </div>
          )}

          {/* Update Username */}
          <form onSubmit={handleUpdateUsername} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Update Username</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700/90 mb-1">Username</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                />
              </div>
              <button
                type="submit"
                className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
              >
                <Save size={16} />
                <span>Update Username</span>
              </button>
            </div>
          </form>

          {/* Update Email */}
          <form onSubmit={handleUpdateEmail} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Update Email</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700/90 mb-1">Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                />
              </div>
              <button
                type="submit"
                className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
              >
                <Save size={16} />
                <span>Update Email</span>
              </button>
            </div>
          </form>

          {/* Update Password */}
          <form onSubmit={handleUpdatePassword} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Update Password</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700/90 mb-1">New Password</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700/90 mb-1">Confirm Password</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                />
              </div>
              <button
                type="submit"
                className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
              >
                <Save size={16} />
                <span>Update Password</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

const cn = (...classes: string[]) => classes.filter(Boolean).join(' ');

export default SettingsPage; 