import React from 'react';

// reword lander copy to be more compelling and less slop-like

const AboutSection = () => {
  return (
    <section className="py-16 bg-gradient-to-br from-catan-brick/5 to-catan-wood/5">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 mb-6">
              About CooperCatan
            </h2>
            <div className="space-y-4 text-gray-600"> 
              <p>
                CooperCatan brings Settlers of Catan into a digital format,maintaining the strategic depth and
                social interaction that made the original game a classic.
              </p>
              <p>
                Whether you're a seasoned Catan player or new to the game, CooperCatan
                provides the perfect platform to build, trade, and compete with players
                from around the world to satisfy your catan cravings.
              </p>
            </div>
          </div>
          <div className="relative">
            <div className="aspect-w-4 aspect-h-3 rounded-xl overflow-hidden shadow-xl">
              <img
                src="/board-icons.png"
                alt="Catan Gameplay"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="absolute -bottom-6 -right-6 w-32 h-32 bg-catan-brick rounded-full opacity-10"></div>
            <div className="absolute -top-6 -left-6 w-24 h-24 bg-catan-wood rounded-full opacity-10"></div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AboutSection;