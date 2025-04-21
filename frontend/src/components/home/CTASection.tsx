import React from 'react';
import { useNavigate } from 'react-router-dom';

const CTASection = () => {
  const navigate = useNavigate();

  return (
    <section className="py-16 bg-gradient-to-br from-catan-brick to-catan-wood relative overflow-hidden">
      <div className="absolute inset-0 bg-black/20"></div>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative">
        <div className="text-center">
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-6">
            Ready to Start Your Journey?
          </h2>
          <p className="text-lg sm:text-xl text-white/90 max-w-2xl mx-auto mb-8">
            Join thousands of players already building their empires in CooperCatan.
            Your first game is just a click away.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <button
              onClick={() => navigate('/signup')}
              className="bg-white text-catan-brick px-8 py-3 rounded-lg font-medium hover:bg-gray-100 transition-colors"
            >
              Create Free Account
            </button>
            <button
              onClick={() => navigate('/signin')}
              className="bg-transparent text-white border-2 border-white px-8 py-3 rounded-lg font-medium hover:bg-white/10 transition-colors"
            >
              Sign In
            </button>
          </div>
        </div>
      </div>
      <div className="absolute -bottom-6 -right-6 w-48 h-48 bg-white/10 rounded-full"></div>
      <div className="absolute -top-6 -left-6 w-32 h-32 bg-white/10 rounded-full"></div>
    </section>
  );
};

export default CTASection;