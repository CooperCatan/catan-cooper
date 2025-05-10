import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, updatePassword, reauthenticateWithCredential, EmailAuthProvider, deleteUser, signOut } from 'firebase/auth';
import { Home, Save, ArrowLeft, Lock, XCircle, Check, X } from 'lucide-react';
import { useAuth } from '../auth/AuthProvider';
import Notification from '../ui/Notification';
import { debounce } from 'lodash';

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
  const { currentUser } = useAuth();
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [notifications, setNotifications] = useState<Array<{
    id: number,
    type: 'success' | 'error',
    text: string
  }>>([]);

  const [username, setUsername] = useState<string>('');
  const [usernamePassword, setUsernamePassword] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [deleteAccountPassword, setDeleteAccountPassword] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isCheckingUsername, setIsCheckingUsername] = useState(false);
  const [isUsernameAvailable, setIsUsernameAvailable] = useState<boolean | null>(null);
  const [usernameError, setUsernameError] = useState<string | null>(null);
  const [passwordsMatch, setPasswordsMatch] = useState<boolean | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // real time username check
  const checkUsername = debounce(async (username: string) => {
    if (!username.trim()) {
      setIsUsernameAvailable(null);
      setUsernameError(null);
      return;
    }

    if (username === account?.username) {
      setIsUsernameAvailable(false);
      setUsernameError('New username cannot be the same as your current username');
      return;
    }

    if (username.length < 3) {
      setIsUsernameAvailable(false);
      setUsernameError('Username must be at least 3 characters');
      return;
    }

    if (username.length > 20) {
      setIsUsernameAvailable(false);
      setUsernameError('Username cannot be longer than 20 characters');
      return;
    }

    setIsCheckingUsername(true);
    try {
      const response = await fetch('http://localhost:8080/api/account/check-username', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
      });

      setIsUsernameAvailable(response.ok);
      setUsernameError(response.ok ? null : 'Username is already taken');
    } catch (error) {
      console.error('Error checking username:', error);
      setUsernameError('Error checking username availability');
      setIsUsernameAvailable(false);
    } finally {
      setIsCheckingUsername(false);
    }
  }, 300);

  // update the username input handler
  const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newUsername = e.target.value;
    setUsername(newUsername);
    checkUsername(newUsername);
  };

  // check passwords match
  const validatePasswords = (newPass: string, confirmPass: string) => {
    if (!newPass && !confirmPass) {
      setPasswordsMatch(null);
      setPasswordError(null);
      return;
    }

    if (newPass.length < 8) {
      setPasswordsMatch(false);
      setPasswordError('Password must be at least 8 characters long');
      return;
    }

    if (!confirmPass) {
      setPasswordsMatch(null);
      setPasswordError(null);
      return;
    }

    const match = newPass === confirmPass;
    setPasswordsMatch(match);
    setPasswordError(match ? null : 'Passwords do not match');
  };

  // update password input handlers
  const handleNewPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newPass = e.target.value;
    setNewPassword(newPass);
    validatePasswords(newPass, confirmPassword);
  };

  const handleConfirmPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const confirmPass = e.target.value;
    setConfirmPassword(confirmPass);
    validatePasswords(newPassword, confirmPass);
  };

  useEffect(() => {
    const loadUserData = async () => {
      if (!currentUser) {
        navigate('/signin');
        return;
      }

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
        addNotification('error', 'Failed to load account data');
      } finally {
        setLoading(false);
      }
    };

    loadUserData();
  }, [currentUser, navigate]);

  const addNotification = (type: 'success' | 'error', text: string) => {
    const id = Date.now();
    setNotifications(prev => [...prev, { id, type, text }]);
  };

  const removeNotification = (id: number) => {
    setNotifications(prev => prev.filter(notification => notification.id !== id));
  };

  const handleUpdateUsername = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!account || !currentUser) return;

    try {
      // check auth
      try {
        const credential = EmailAuthProvider.credential(currentUser.email!, usernamePassword);
        await reauthenticateWithCredential(currentUser, credential);
      } catch (authError: any) {
        addNotification('error', 'Incorrect password. Please try again.');
        return;
      }

      // update user api call
      const idToken = await currentUser.getIdToken();
      const response = await fetch('http://localhost:8080/api/account/username', {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${idToken}`
        },
        body: JSON.stringify({ newUsername: username })
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        if (errorData?.message?.includes('already exists')) {
          addNotification('error', 'Username is already taken!');
        } else {
          addNotification('error', 'Failed to update username. Please try again.');
        }
        return;
      }

      // update account state
      const updatedAccount = await response.json();
      setAccount(updatedAccount);
      addNotification('success', 'Username successfully updated');
      setUsernamePassword('');
      setUsername('');
    } catch (error: any) {
      console.error('Error updating username:', error);
      addNotification('error', 'Failed to update username. Please try again.');
    }
  };

  const handleUpdatePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;

    try {
      // check auth
      try {
        const credential = EmailAuthProvider.credential(currentUser.email!, currentPassword);
        await reauthenticateWithCredential(currentUser, credential);
      } catch (authError: any) {
        addNotification('error', 'Incorrect password. Please try again.');
        return;
      }

      // update password w Firebase
      try {
        await updatePassword(currentUser, newPassword);
        addNotification('success', 'Password successfully updated');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      } catch (error: any) {
        console.error('Error updating password:', error);
        if (error.code === 'auth/requires-recent-login') {
          addNotification('error', 'Session expired. Please sign out and sign in again.');
        } else {
          addNotification('error', 'Failed to update password. Please try again.');
        }
      }
    } catch (error: any) {
      console.error('Error updating password:', error);
      if (error.code === 'auth/requires-recent-login') {
        addNotification('error', 'Session expired. Please sign out and sign in again.');
      } else {
        addNotification('error', 'Failed to update password. Please try again.');
      }
    }
  };

  const handleDeleteAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser || !account || isDeleting) return;

    try {
      setIsDeleting(true);
      
      // auth
      try {
        const credential = EmailAuthProvider.credential(currentUser.email!, deleteAccountPassword);
        await reauthenticateWithCredential(currentUser, credential);
      } catch (authError: any) {
        addNotification('error', 'Incorrect password. Please try again.');
        setIsDeleting(false);
        return;
      }

      // delete from postgres first
      const response = await fetch('http://localhost:8080/api/account', {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${await currentUser.getIdToken()}`
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        addNotification('error', errorData?.message || 'Failed to delete account. Please try again.');
        setIsDeleting(false);
        return;
      }

      // show success notification first
      addNotification('success', 'Account successfully deleted');
      
      // wait
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // then delete from Firebase and redirect to sign-in page (automatically done
      // by firebase)
      await deleteUser(currentUser);
      navigate('/');
      
    } catch (error: any) {
      console.error('Error deleting account:', error);
      setIsDeleting(false);
      if (error.code === 'auth/requires-recent-login') {
        addNotification('error', 'Session expired. Please sign out and sign in again.');
      } else {
        addNotification('error', 'Failed to delete account. Please try again.');
      }
    }
  };

  useEffect(() => {
    if (!currentUser && isDeleting) {
      navigate('/');
    }
  }, [currentUser, isDeleting, navigate]);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex flex-col overflow-hidden">
      {/* Notifications */}
      {notifications.map(notification => (
        <Notification
          key={notification.id}
          type={notification.type}
          message={notification.text}
          onClose={() => removeNotification(notification.id)}
        />
      ))}

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
          <div className="flex items-center space-x-4">
            {account && (
              <div className="flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/20">
                <span className="text-gray-700/90 font-medium">{account.username}</span>
                <span className="text-sm text-gray-600/90">ELO {account.elo}</span>
              </div>
            )}
            <button
              onClick={async () => {
                await signOut(auth);
                navigate('/');
              }}
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
        <div className="max-w-2xl mx-auto space-y-6">
          {/* Account Stats */}
          {account && (
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20 shadow-lg hover:shadow-xl transition-all">
              <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Account Info</h2>
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2 p-4 bg-white/10 rounded-lg border border-white/10 backdrop-blur-sm transition-all hover:bg-white/20">
                  <div className="text-sm text-gray-600/90">Email</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.email}</div>
                </div>
                <div className="p-4 bg-white/10 rounded-lg border border-white/10 backdrop-blur-sm transition-all hover:bg-white/20">
                  <div className="text-sm text-gray-600/90">Total Games</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.totalGames}</div>
                </div>
                <div className="p-4 bg-white/10 rounded-lg border border-white/10 backdrop-blur-sm transition-all hover:bg-white/20">
                  <div className="text-sm text-gray-600/90">Win Rate</div>
                  <div className="text-lg font-medium text-gray-800/90">
                    {((account.totalWins / (account.totalGames || 1)) * 100).toFixed(1)}%
                  </div>
                </div>
                <div className="p-4 bg-white/10 rounded-lg border border-white/10 backdrop-blur-sm transition-all hover:bg-white/20">
                  <div className="text-sm text-gray-600/90">ELO Rating</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.elo}</div>
                </div>
                <div className="p-4 bg-white/10 rounded-lg border border-white/10 backdrop-blur-sm transition-all hover:bg-white/20">
                  <div className="text-sm text-gray-600/90">Total Wins</div>
                  <div className="text-lg font-medium text-gray-800/90">{account.totalWins}</div>
                </div>
              </div>
            </div>
          )}

          {/* Update Username */}
          <form onSubmit={handleUpdateUsername} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20 shadow-lg hover:shadow-xl transition-all">
            <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Update Username</h2>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">Current Username</label>
                <div className="px-4 py-2 bg-white/10 border border-white/10 rounded-lg text-gray-700/90 backdrop-blur-sm">
                  {account?.username}
                </div>
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">New Username</label>
                <div className="relative">
                  <input
                    type="text"
                    value={username}
                    onChange={handleUsernameChange}
                    autoComplete="off"
                    name="new-username"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-white/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Enter your new username"
                  />
                  {username && !isCheckingUsername && (
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                      {isUsernameAvailable ? (
                        <Check className="w-5 h-5 text-green-500" />
                      ) : (
                        <X className="w-5 h-5 text-red-500" />
                      )}
                    </div>
                  )}
                </div>
                <div className="h-5">
                  {usernameError && (
                    <p className="text-sm text-red-500">{usernameError}</p>
                  )}
                  {isUsernameAvailable && (
                    <p className="text-sm text-green-500">Username is available!</p>
                  )}
                </div>
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">Current Password</label>
                <div className="relative">
                  <input
                    type="password"
                    value={usernamePassword}
                    onChange={(e) => setUsernamePassword(e.target.value)}
                    autoComplete="new-password"
                    name="current-password-for-username"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-white/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Enter your current password"
                  />
                  <Lock className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
                </div>
              </div>
              <button
                type="submit"
                disabled={!isUsernameAvailable || username === account?.username || !usernamePassword}
                className={cn(
                  "flex items-center gap-2 px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all shadow-lg border w-full justify-center",
                  !isUsernameAvailable || username === account?.username || !usernamePassword
                    ? "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
                    : "bg-white/20 text-gray-800/90 hover:bg-white/30 hover:shadow-xl border-white/20"
                )}
              >
                <Save size={16} />
                <span>Update Username</span>
              </button>
            </div>
          </form>

          {/* Update Password */}
          <form onSubmit={handleUpdatePassword} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-white/20 shadow-lg hover:shadow-xl transition-all">
            <h2 className="text-lg font-semibold text-gray-800/90 mb-4">Update Password</h2>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">Current Password</label>
                <div className="relative">
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    autoComplete="new-password"
                    name="current-password-for-update"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-white/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Enter your current password"
                  />
                  <Lock className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
                </div>
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">New Password</label>
                <div className="relative">
                  <input
                    type="password"
                    value={newPassword}
                    onChange={handleNewPasswordChange}
                    autoComplete="new-password"
                    name="new-password"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-white/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Enter your new password"
                  />
                  {newPassword && (
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                      {newPassword.length >= 8 ? (
                        <Check className="w-5 h-5 text-green-500" />
                      ) : (
                        <X className="w-5 h-5 text-red-500" />
                      )}
                    </div>
                  )}
                </div>
                <div className="h-5">
                  {passwordError && (
                    <p className="text-sm text-red-500">{passwordError}</p>
                  )}
                </div>
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700/90">Confirm Password</label>
                <div className="relative">
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={handleConfirmPasswordChange}
                    autoComplete="new-password"
                    name="confirm-new-password"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-white/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Confirm your new password"
                  />
                  {confirmPassword && (
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                      {passwordsMatch ? (
                        <Check className="w-5 h-5 text-green-500" />
                      ) : (
                        <X className="w-5 h-5 text-red-500" />
                      )}
                    </div>
                  )}
                </div>
                <div className="h-5">
                  {passwordError && confirmPassword && !passwordsMatch && (
                    <p className="text-sm text-red-500">{passwordError}</p>
                  )}
                  {passwordsMatch && newPassword && (
                    <p className="text-sm text-green-500">Passwords match!</p>
                  )}
                </div>
              </div>
              <button
                type="submit"
                disabled={!currentPassword || !passwordsMatch || !newPassword || !confirmPassword}
                className={cn(
                  "flex items-center gap-2 px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all shadow-lg border w-full justify-center",
                  !currentPassword || !passwordsMatch || !newPassword || !confirmPassword
                    ? "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
                    : "bg-white/20 text-gray-800/90 hover:bg-white/30 hover:shadow-xl border-white/20"
                )}
              >
                <Save size={16} />
                <span>Update Password</span>
              </button>
            </div>
          </form>

          {/* Delete Account */}
          <form onSubmit={handleDeleteAccount} className="bg-white/20 backdrop-blur-sm rounded-xl p-6 border border-red-200/20 shadow-lg hover:shadow-xl transition-all">
            <h2 className="text-lg font-semibold text-red-800/90 mb-4">Delete Account</h2>
            <div className="space-y-4">
              <div className="p-4 bg-red-100/30 rounded-lg border border-red-200/30 text-red-700/90">
                Warning: This action cannot be undone. Your account and all associated data will be permanently deleted.
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700/90 mb-1">Confirm Password</label>
                <div className="relative">
                  <input
                    type="password"
                    value={deleteAccountPassword}
                    onChange={(e) => setDeleteAccountPassword(e.target.value)}
                    autoComplete="new-password"
                    name="password-for-delete"
                    className="w-full px-4 py-2 bg-white/10 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500/30 text-gray-700/90 backdrop-blur-sm placeholder:text-gray-500/50"
                    placeholder="Enter your password to confirm"
                  />
                  <Lock className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
                </div>
              </div>
              <div className="flex gap-4">
                <button
                  type="button"
                  onClick={() => setShowDeleteConfirm(true)}
                  disabled={!deleteAccountPassword}
                  className={cn(
                    "flex-1 flex items-center gap-2 px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all shadow-lg border justify-center",
                    !deleteAccountPassword
                      ? "bg-gray-100/50 text-gray-400 cursor-not-allowed border-gray-200/50"
                      : "bg-red-100/20 text-red-800/90 hover:bg-red-100/30 hover:shadow-xl border-red-200/20"
                  )}
                >
                  Delete Account
                </button>
              </div>
            </div>
          </form>

          {/* Delete Confirmation Modal */}
          {showDeleteConfirm && (
            <div className="fixed inset-0 flex items-center justify-center p-4 z-50">
              <div className="bg-white rounded-2xl p-8 max-w-md w-full shadow-2xl border border-gray-100">
                <h3 className="text-xl font-semibold text-gray-800 mb-2">
                  Confirm Account Deletion
                </h3>
                <p className="text-gray-600 mb-6">
                  Are you sure you want to delete your account? This action cannot be undone.
                </p>
                <div className="flex gap-3">
                  <button
                    type="button"
                    onClick={() => setShowDeleteConfirm(false)}
                    className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    onClick={(e) => {
                      setShowDeleteConfirm(false);  
                      handleDeleteAccount(e);
                    }}
                    className="flex-1 px-4 py-2 bg-red-500 text-white rounded-xl hover:bg-red-600 transition-colors font-medium"
                  >
                    Delete Account
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

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

export default SettingsPage; 