import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { initializeApp } from "firebase/app";
import { setPersistence, browserLocalPersistence, getAuth } from "firebase/auth";
import reportWebVitals from './reportWebVitals';

import App from "./App";
import SignInPage from "./components/auth/SignInPage";
import SignUpPage from "./components/auth/SignUpPage";
import GameLobby from "./components/game/GameLobby";
import GameRoom from "./components/game/GameRoom";
import SettingsPage from "./components/settings/SettingsPage";
import AuthProvider from "./components/auth/AuthProvider";

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.REACT_APP_FIREBASE_APP_ID
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Set persistence to local
const auth = getAuth(app);
setPersistence(auth, browserLocalPersistence);

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/signin" element={<SignInPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/lobby" element={<GameLobby />} />
          <Route path="/game/:gameId" element={<GameRoom />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);

reportWebVitals();
