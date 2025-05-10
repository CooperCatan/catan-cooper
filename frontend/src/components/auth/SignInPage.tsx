import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';
import { Home, ArrowRight } from 'lucide-react';

const SignInPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const auth = getAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      // check if email in db
      const checkEmailResponse = await fetch('http://localhost:8080/api/account/check-email', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email })
      });

      // email is NOT found (available for signup)
      if (checkEmailResponse.ok) {
        setError('Account not found. Please Sign Up.');
        return;
      }

      // email exists, attempt auth w Firebase
      try {
        const userCredential = await signInWithEmailAndPassword(auth, email, password);
        const idToken = await userCredential.user.getIdToken();
        
        // fetch acct details from db
        const response = await fetch('http://localhost:8080/api/account/by-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Authorization': `Bearer ${idToken}`
          },
          body: JSON.stringify({ email })
        });
        // generic error w db
        if (!response.ok) {
          const errorData = await response.text();
          console.error('Backend error:', errorData);
          setError('Failed to fetch account details');
          await auth.signOut();
          return;
        }
        // success redir to lobby
        const accountDetails = await response.json();
        navigate('/lobby');
      } catch (authError: any) {
        console.error('Authentication error:', authError);
        
        // email exists but Firebase auth failed --> password must be incorrect
        if (authError.code === 'auth/invalid-credential') {
          setError('Incorrect password. Please try again.');
          return;
        }
        
        if (authError.code === 'auth/too-many-requests') {
          setError('Too many failed attempts. Please try again later.');
          return;
        }
        
        setError('An error occurred during sign in. Please try again.');
      }
    } catch (error: any) {
      console.error('Error checking email:', error);
      setError('An error occurred. Please try again.');
    }
  };

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
            <h1 className="text-2xl font-bold text-gray-800/90">Sign In</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md space-y-6">
          <div className="relative">
            {error && (
              <div className="absolute -top-16 left-0 right-0 p-4 bg-red-100/50 backdrop-blur-sm border border-red-200/50 rounded-xl text-red-600 text-sm">
                {error}
              </div>
            )}

            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-8 border border-white/20 shadow-xl">
              <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700/90 mb-1">
                    Email
                  </label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="off"
                    name="signin-email"
                    className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700/90 mb-1">
                    Password
                  </label>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    name="signin-password"
                    className="w-full px-4 py-2 bg-white/50 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-gray-800/90 font-medium rounded-xl hover:bg-white/30 transition-all shadow-lg hover:shadow-xl border border-white/20"
                >
                  <span>Sign In</span>
                  <ArrowRight size={16} />
                </button>
              </form>

              <div className="mt-6 text-center">
                <p className="text-sm text-gray-600/90">
                  Don't have an account?{' '}
                  <button
                    onClick={() => navigate('/signup')}
                    className="text-blue-600/90 hover:text-blue-700/90 font-medium"
                  >
                    Sign Up
                  </button>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignInPage;
