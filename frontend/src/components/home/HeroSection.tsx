import React from 'react';
import { useNavigate } from 'react-router-dom';

const HeroSection = () => {
  const navigate = useNavigate();

  return (
    <section className="relative min-h-screen flex items-center justify-center">
      {/* Background Image */}
      <div 
        className="absolute inset-0 bg-cover bg-center bg-no-repeat"
        style={{ 
          backgroundImage: 'url(/hero-bg.png)',
          backgroundPosition: 'center 20%'
        }}
      >
        <div className="absolute inset-0 bg-black/30"></div>
      </div>

      {/* Content */}
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 text-center">
        <div className="space-y-8">
          <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold text-white mb-6">
            Build Your Empire in
            <span className="text-catan-brick block mt-2 italic">Cooper Union's Catan</span>
          </h1>
          <p className="text-lg sm:text-xl text-gray-600 max-w-2xl mx-auto mb-8">
            Trade, build, and compete
            with players from around the world in a modern digital adaptation.
          </p>
          <div className="flex justify-center gap-4">
            <button
              onClick={() => navigate('/signup')}
              className="bg-catan-brick text-white px-8 py-3 rounded-lg font-medium hover:bg-catan-brick/90 transition-colors"
            >
              Start Playing Free
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;