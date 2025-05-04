import React, { type ReactElement } from 'react';
import './App.css';
import Navbar from './components/home/Navbar';
import HeroSection from './components/home/HeroSection';
import FeaturesSection from './components/home/FeaturesSection';
import AboutSection from './components/home/AboutSection';
import CTASection from './components/home/CTASection';

const App = (): ReactElement => {
  return (
    <div className="app">
      <Navbar />
      <main>
        <HeroSection />
        <FeaturesSection />
        <AboutSection />
        <CTASection />
      </main>
    </div>
  );
};

export default App;