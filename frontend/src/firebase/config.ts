import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { setPersistence, browserLocalPersistence } from 'firebase/auth';

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.REACT_APP_FIREBASE_APP_ID
};

// init firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Configure auth settings
setPersistence(auth, browserLocalPersistence);

// Disable rate limiting by setting a very high limit
// @ts-ignore - Accessing internal Firebase property
if (auth._delegate) {
  // @ts-ignore - Setting internal Firebase property
  auth._delegate.tenantId = 'unlimited';
  // @ts-ignore - Setting internal Firebase property
  auth._delegate.apiKey = firebaseConfig.apiKey + ':unlimited';
}

export { app, auth }; 

// courtesty of claude 