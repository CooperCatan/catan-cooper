/// <reference types="react" />

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, createUserWithEmailAndPassword } from 'firebase/auth';
import { Home, ArrowRight, Check, X } from 'lucide-react';
import { debounce } from 'lodash';

const SignUpPage = () => {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [usernameError, setUsernameError] = useState<string | null>(null);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmPasswordError, setConfirmPasswordError] = useState<string | null>(null);
  const [isUsernameAvailable, setIsUsernameAvailable] = useState<boolean | null>(null);
  const [isEmailAvailable, setIsEmailAvailable] = useState<boolean | null>(null);
  const [isPasswordValid, setIsPasswordValid] = useState<boolean | null>(null);
  const [isCheckingUsername, setIsCheckingUsername] = useState(false);
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);
  const navigate = useNavigate();
  const auth = getAuth();

  // username minimum and maximum char validation
  const checkUsername = debounce(async (username: string) => {
    if (!username || username.length < 3) {
      setUsernameError('Username must be at least 3 characters');
      setIsUsernameAvailable(false);
      return;
    }

    if (username.length > 20) {
      setUsernameError('Username cannot exceed 20 characters');
      setIsUsernameAvailable(false);
      return;
    }

    // check if username is taken in real-time
    setIsCheckingUsername(true);
    try {
      const response = await fetch('http://localhost:8080/api/account/check-username', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username }),
      });

      if (response.ok) {
        setUsernameError(null);
        setIsUsernameAvailable(true);
      } else {
        setUsernameError('Username is already taken');
        setIsUsernameAvailable(false);
      }
    } catch (error) {
      setUsernameError('Error checking username availability');
      setIsUsernameAvailable(false);
    } finally {
      setIsCheckingUsername(false);
    }
  }, 500);

  const checkEmail = debounce(async (email: string) => {
    // basic email format validation w regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email || !emailRegex.test(email)) {
      setEmailError('Please enter a valid email');
      setIsEmailAvailable(false);
      return;
    }

    // check if email is taken in real-time
    setIsCheckingEmail(true);
    try {
      const response = await fetch('http://localhost:8080/api/account/check-email', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      
      if (response.ok) {
        // email is available (not taken)
        setEmailError(null);
        setIsEmailAvailable(true);
      } else {
        const errorText = await response.text();
        if (response.status === 400) {
          // email is not available (taken)
          setEmailError('Email is already registered');
          setIsEmailAvailable(false);
        } else {
          throw new Error(`Server error: ${errorText}`);
        }
      }
      // generic error w db
    } catch (error) {
      console.error('Email check error:', error);
      setEmailError('Error checking email availability');
      setIsEmailAvailable(false);
    } finally {
      setIsCheckingEmail(false);
    }
  }, 500);

  // clean up 
  useEffect(() => {
    return () => {
      checkUsername.cancel();
      checkEmail.cancel();
    };
  }, []);

  // validation triggers
  useEffect(() => {
    if (username) {
      checkUsername(username);
    } else {
      setIsUsernameAvailable(null);
      setUsernameError(null);
    }
  }, [username]);

  useEffect(() => {
    if (email) {
      checkEmail(email);
    } else {
      setIsEmailAvailable(null);
      setEmailError(null);
    }
  }, [email]);

  // password minimum char validation
  useEffect(() => {
    if (password) {
      if (password.length < 8) {
        setPasswordError('Password must be at least 8 characters');
        setIsPasswordValid(false);
      } else {
        setPasswordError(null);
        setIsPasswordValid(true);
      }
    } else {
      setPasswordError(null);
      setIsPasswordValid(null);
    }
  }, [password]);

  // confirm password matching
  useEffect(() => {
    if (confirmPassword) {
      if (password !== confirmPassword) {
        setConfirmPasswordError('Passwords do not match');
      } else {
        setConfirmPasswordError(null);
      }
    } else {
      setConfirmPasswordError(null);
    }
  }, [password, confirmPassword]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!isUsernameAvailable || !isEmailAvailable || !isPasswordValid) {
      setError('Please fix the errors before submitting');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    try {
      // create Firebase account, should be fine if user passed all cases above to do first before postgres add
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      const idToken = await userCredential.user.getIdToken();
      
      // create account in postgres
      const response = await fetch('http://localhost:8080/api/account', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${idToken}`
        },
        body: JSON.stringify({
          username,
          email
        }),
      });

      if (!response.ok) {
        // postgres creation fails, delete the Firebase account to keep in sync
        await userCredential.user.delete();
        const errorData = await response.text();
        console.error('Backend error:', errorData);
        setError('Failed to create account');
        return;
      }

      // success redir to lobby
      navigate('/lobby');
    } catch (error: any) {
      console.error('Error signing up:', error);
      setError(error.message);
    }
  };

  const getInputStyle = (isAvailable: boolean | null, isChecking: boolean) => {
    if (isChecking) return "bg-yellow-50/50";
    if (isAvailable === null) return "bg-white/50";
    return isAvailable ? "bg-green-50/50" : "bg-red-50/50";
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
            <h1 className="text-2xl font-bold text-gray-800/90">Sign Up</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md space-y-6">
          {error && (
            <div className="p-4 bg-red-100/50 backdrop-blur-sm border border-red-200/50 rounded-xl text-red-600 text-sm">
              {error}
            </div>
          )}

          <div className="bg-white/20 backdrop-blur-sm rounded-xl p-8 border border-white/20 shadow-xl">
            <form onSubmit={handleSubmit} className="space-y-8">
              <div className="relative mb-8">
                <label className="block text-sm font-medium text-gray-700/90 mb-1">
                  Email
                </label>
                <div className="relative">
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="off"
                    name="signup-email"
                    className={`w-full px-4 py-2 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 pr-10 ${getInputStyle(isEmailAvailable, isCheckingEmail)}`}
                    required
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3">
                    {isCheckingEmail && (
                      <div className="animate-spin h-5 w-5 border-2 border-blue-500/50 rounded-full border-t-transparent"/>
                    )}
                    {!isCheckingEmail && isEmailAvailable === true && (
                      <Check className="h-5 w-5 text-green-500" />
                    )}
                    {!isCheckingEmail && isEmailAvailable === false && (
                      <X className="h-5 w-5 text-red-500" />
                    )}
                  </div>
                  <div className="absolute top-full left-0 min-h-[20px] pt-1">
                    {emailError && (
                      <p className="text-sm text-red-600">{emailError}</p>
                    )}
                  </div>
                </div>
              </div>
              <div className="relative mb-8">
                <label className="block text-sm font-medium text-gray-700/90 mb-1">
                  Username
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="off"
                    name="signup-username"
                    className={`w-full px-4 py-2 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 pr-10 ${getInputStyle(isUsernameAvailable, isCheckingUsername)}`}
                    required
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3">
                    {isCheckingUsername && (
                      <div className="animate-spin h-5 w-5 border-2 border-blue-500/50 rounded-full border-t-transparent"/>
                    )}
                    {!isCheckingUsername && isUsernameAvailable === true && (
                      <Check className="h-5 w-5 text-green-500" />
                    )}
                    {!isCheckingUsername && isUsernameAvailable === false && (
                      <X className="h-5 w-5 text-red-500" />
                    )}
                  </div>
                  <div className="absolute top-full left-0 min-h-[20px] pt-1">
                    {usernameError && (
                      <p className="text-sm text-red-600">{usernameError}</p>
                    )}
                  </div>
                </div>
              </div>
              <div className="relative mb-8">
                <label className="block text-sm font-medium text-gray-700/90 mb-1">
                  Password
                </label>
                <div className="relative">
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    name="signup-new-password"
                    className={`w-full px-4 py-2 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 pr-10 ${
                      password ? (isPasswordValid ? 'bg-green-50/50' : 'bg-red-50/50') : 'bg-white/50'
                    }`}
                    required
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3">
                    {password && (isPasswordValid ? (
                      <Check className="h-5 w-5 text-green-500" />
                    ) : (
                      <X className="h-5 w-5 text-red-500" />
                    ))}
                  </div>
                  <div className="absolute top-full left-0 min-h-[20px] pt-1">
                    {passwordError && (
                      <p className="text-sm text-red-600">{passwordError}</p>
                    )}
                  </div>
                </div>
              </div>
              <div className="relative mb-8">
                <label className="block text-sm font-medium text-gray-700/90 mb-1">
                  Confirm Password
                </label>
                <div className="relative">
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                    name="signup-confirm-password"
                    className={`w-full px-4 py-2 border border-white/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 pr-10 ${
                      confirmPassword ? (confirmPassword === password ? 'bg-green-50/50' : 'bg-red-50/50') : 'bg-white/50'
                    }`}
                    required
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3">
                    {confirmPassword && (confirmPassword === password ? (
                      <Check className="h-5 w-5 text-green-500" />
                    ) : (
                      <X className="h-5 w-5 text-red-500" />
                    ))}
                  </div>
                  <div className="absolute top-full left-0 min-h-[20px] pt-1">
                    {confirmPasswordError && (
                      <p className="text-sm text-red-600">{confirmPasswordError}</p>
                    )}
                  </div>
                </div>
              </div>
              <button
                type="submit"
                disabled={!isUsernameAvailable || !isEmailAvailable || !isPasswordValid || password !== confirmPassword}
                className={`w-full flex items-center justify-center gap-2 px-4 py-2 backdrop-blur-sm font-medium rounded-xl transition-all shadow-lg border ${
                  (!isUsernameAvailable || !isEmailAvailable || !isPasswordValid || password !== confirmPassword)
                    ? 'bg-gray-200/50 text-gray-400 cursor-not-allowed border-gray-200/50 hover:shadow-lg opacity-50'
                    : 'bg-white/20 text-gray-800/90 hover:bg-white/30 hover:shadow-xl border-white/20'
                }`}
              >
                <span>Create Account</span>
                <ArrowRight size={16} className={!isUsernameAvailable || !isEmailAvailable || !isPasswordValid || password !== confirmPassword ? 'opacity-50' : ''} />
              </button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-gray-600/90">
                Already have an account?{' '}
                <button
                  onClick={() => navigate('/signin')}
                  className="text-blue-600/90 hover:text-blue-700/90 font-medium"
                >
                  Sign In
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignUpPage; 