import React, { type ReactElement } from 'react';
import { useNavigate } from 'react-router-dom';

const Navbar = (): ReactElement => {
  const navigate = useNavigate();

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const navigateToSignIn = () => {
    navigate('/signin');
  };

  return (
    <header className="fixed top-0 w-full bg-white/80 backdrop-blur-md border-b z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <div className="text-2xl font-bold text-catan-brick cursor-pointer" onClick={scrollToTop}>
          CooperCatan
        </div>
        <nav className="flex gap-4">
          <button 
            onClick={scrollToTop}
            className="px-4 py-2 text-catan-brick hover:bg-catan-brick/10 rounded-lg transition-colors"
            type="button"
          >
            Home
          </button>
          <button 
            onClick={navigateToSignIn}
            className="px-4 py-2 bg-catan-brick text-white hover:bg-catan-brick/90 rounded-lg transition-colors"
            type="button"
          >
            Sign In
          </button>
        </nav>
      </div>
    </header>
  );
};

export default Navbar;