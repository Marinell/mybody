import React, { useState, useEffect } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom'; // Added useNavigate
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [userType, setUserType] = useState('client'); // Default to client
  const [message, setMessage] = useState('');
  const { login, isLoggedIn, currentUser, isAuthLoading } = useAuth(); // Get login from context
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const type = queryParams.get('userType');
    if (type) {
      setUserType(type);
    }
  }, [location.search]);

  useEffect(() => {
    // Redirect if already logged in and auth is not loading
    if (!isAuthLoading && isLoggedIn() && currentUser) {
        if (currentUser.role === 'PROFESSIONAL') {
            navigate('/professional-dashboard');
        } else if (currentUser.role === 'CLIENT') {
            navigate('/customer-request');
        } else {
            navigate('/'); // Fallback if role is somehow different
        }
    }
  }, [isLoggedIn, currentUser, navigate, isAuthLoading]);


  const handleLogin = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      await login(email, password, userType);
      // Navigation is handled by the login function in AuthContext
    } catch (error) {
      setMessage(error.data?.message || error.message || 'Login failed.');
    }
  };

  // Simplified JSX structure, focusing on functionality
  // Omitting complex Tailwind classes for now
  // Added basic styling similar to previous components
  return (
    // Body styles are applied globally, so this div might not need specific classes for font, bg, color, minHeight if they are inherited.
    // However, explicit flex column might be needed if not default.
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <header className="app-header">
        <Link to="/" className="app-header-title"><div><h2>FitConnect</h2></div></Link>
      </header>
      <div className="main-container">
        <div className="content-container">
          <h2 className="form-title">
            Login {userType && `- ${userType.charAt(0).toUpperCase() + userType.slice(1)}`}
          </h2>
          {message && <div className="error-message">{message}</div>}
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label htmlFor="email" className="form-label">Email</label>
              <input
                type="email"
                id="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="password" className="form-label">Password</label>
              <input
                type="password"
                id="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-input"
              />
            </div>
            <p className="form-text-link">
              <Link to="/forgot-password">Forgot password?</Link>
            </p>
            <div style={{paddingTop: '0.5rem'}}> {/* This div might need a class if padding is recurrent */}
              <button
                type="submit"
                className="form-button"
              >
                Log in
              </button>
            </div>
            <p className="form-subtext">
              Don't have an account?
              {userType === 'client'
                ? <Link to="/signup-client"> Sign up as Client</Link> // global 'a' style applies
                : <Link to="/signup-professional"> Sign up as Professional</Link>}
            </p>
            <p className="form-subtext">
              Or go back to <Link to="/">User Type Selection</Link>.
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
