import React from 'react';
import NavBar from './components/navigation/NavBar';
import HeroSection from './components/home/HeroSection';
import FeaturesSection from './components/home/FeaturesSection';
import AboutSection from './components/home/AboutSection';
import CTASection from './components/home/CTASection';

function App() {
  return (
    <div className="min-h-screen">
      <NavBar />
      <main>
        <HeroSection />
        <FeaturesSection />
        <AboutSection />
        <CTASection />
      </main>
    </div>
  );
}

export default App;