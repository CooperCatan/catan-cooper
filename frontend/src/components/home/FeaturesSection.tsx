import React from 'react';

const features = [
  {
    title: 'Real-Time Multiplayer',
    description: 'Play with friends or match with players worldwide in real-time matches.',
    icon: '🌐'
  },
  {
    title: 'Classic Rules',
    description: 'Experience the authentic Catan gameplay you know and love.',
    icon: '📜'
  },
  {
    title: 'Ranked Matches',
    description: 'Compete in ranked games and climb the global leaderboard.',
    icon: '🏆'
  },
  {
    title: 'Custom Games',
    description: 'Create private games with custom rules and invite your friends.',
    icon: '⚙️'
  }
];

const FeaturesSection = () => {
  return (
    <section className="py-16 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            The Ultimate Digital Catan Experience
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Everything you love about Catan, enhanced with modern features for online play.
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <div
              key={index}
              className="bg-gradient-to-br from-catan-brick/5 to-catan-wood/5 p-6 rounded-xl hover:shadow-lg transition-shadow"
            >
              <div className="text-4xl mb-4">{feature.icon}</div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                {feature.title}
              </h3>
              <p className="text-gray-600">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;