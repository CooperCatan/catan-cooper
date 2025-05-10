import React, { createContext, useContext, useEffect, useState } from 'react';
import { getAuth, onAuthStateChanged, User } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
  currentUser: User | null;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType>({ currentUser: null, loading: true });

export const useAuth = () => useContext(AuthContext);

// no auth routes
const PUBLIC_ROUTES = ['/', '/signin', '/signup'];

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const auth = getAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setCurrentUser(user);
      setLoading(false);

      const currentPath = window.location.pathname;
      const isPublicRoute = PUBLIC_ROUTES.some(route => currentPath === route);

      // redirect to signin if user is not authenticated and trying to access a protected route
      if (!user && !isPublicRoute) {
        navigate('/signin');
      }
    });

    return unsubscribe;
  }, [auth, navigate]);

  const value = {
    currentUser,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export default AuthProvider; 