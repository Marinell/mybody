import React from 'react';
import ReactDOM from 'react-dom/client';
import { Router, BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './context/AuthContext'; // Import AuthProvider
// import './index.css'; // Optional

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
  <BrowserRouter>
    <AuthProvider> {/* Wrap App with AuthProvider */}
       <App />
    </AuthProvider>
   </BrowserRouter>
  </React.StrictMode>
);
