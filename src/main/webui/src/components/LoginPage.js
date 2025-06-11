import React, { useState, useEffect } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom'; // Added useNavigate
import { useAuth } from '../../context/AuthContext';

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
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: '#101323', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #21284a', padding: '0.75rem 2.5rem' }}>
        <Link to="/" style={{textDecoration: 'none', color: 'white'}}><div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}><h2>FitConnect</h2></div></Link>
      </header>
      <div style={{ display: 'flex', flex: 1, justifyContent: 'center', alignItems: 'center', padding: '1.25rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', width: '100%', maxWidth: '512px', backgroundColor: '#181d35', padding: '2rem', borderRadius: '0.75rem' }}>
          <h2 style={{ fontSize: '28px', fontWeight: 'bold', textAlign: 'center', paddingBottom: '0.75rem', paddingTop: '1.25rem' }}>
            Login {userType && `- ${userType.charAt(0).toUpperCase() + userType.slice(1)}`}
          </h2>
          {message && <div style={{ color: 'red', textAlign: 'center', padding: '0.5rem', marginBottom: '1rem' }}>{message}</div>}
          <form onSubmit={handleLogin}>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="email" style={{ display: 'block', marginBottom: '0.5rem' }}>Email</label>
              <input
                type="email"
                id="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="password" style={{ display: 'block', marginBottom: '0.5rem' }}>Password</label>
              <input
                type="password"
                id="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <p style={{ color: '#8e99cc', fontSize: '0.875rem', padding: '0.25rem 0', textDecoration: 'underline', cursor: 'pointer', marginBottom: '1rem' }}>
              <Link to="/forgot-password">Forgot password?</Link>
            </p>
            <div style={{paddingTop: '0.5rem'}}>
              <button
                type="submit"
                style={{ width: '100%', padding: '0.75rem 1.25rem', borderRadius: '9999px', backgroundColor: '#607afb', color: 'white', fontWeight: 'bold', cursor: 'pointer', border: 'none' }}
              >
                Log in
              </button>
            </div>
            <p style={{ color: '#8e99cc', fontSize: '0.875rem', textAlign: 'center', padding: '1rem 0 0.25rem' }}>
              Don't have an account?
              {userType === 'client'
                ? <Link to="/signup-client" className="underline hover:text-white"> Sign up as Client</Link>
                : <Link to="/signup-professional" className="underline hover:text-white"> Sign up as Professional</Link>}
            </p>
            <p style={{ color: '#8e99cc', fontSize: '0.875rem', textAlign: 'center', padding: '0.25rem 0' }}>
              Or go back to <Link to="/" className="underline hover:text-white">User Type Selection</Link>.
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
