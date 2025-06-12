import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Import useAuth

const SignupClientPage = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
  });
  const [message, setMessage] = useState('');
  const { signup } = useAuth(); // Get signup from context
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!formData.name || !formData.email || !formData.password) {
        setMessage("Full Name, email, and password are required.");
        return;
    }

    if (formData.password !== formData.confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    try {
      const { confirmPassword, ...payload } = formData; // Exclude confirmPassword from payload
      await signup(payload, 'client'); // Use signup from context
      setMessage('Signup successful! Please login.');
      setTimeout(() => navigate('/login?userType=client'), 2000);
    } catch (error) {
      setMessage(error.data?.message || error.message || 'Signup failed. Please try again.');
    }
  };

  // Simplified JSX, focusing on functionality. Tailwind classes omitted for brevity.
  // Styles are inline for consistency with previous conversions.
  return (
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: '#101323', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #21284a', padding: '0.75rem 2.5rem' }}>
      <Link to="/" style={{textDecoration: 'none', color: 'white'}}><div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}><h2>FitConnect</h2></div></Link>
        <Link to="/login?userType=client" style={{ color: 'white', fontSize: '0.875rem', fontWeight: '500', textDecoration: 'none' }}>Log In</Link>
      </header>
      <main style={{ display: 'flex', flex: 1, justifyContent: 'center', alignItems: 'center', padding: '1.25rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', width: '100%', maxWidth: '28rem', backgroundColor: '#181d35', padding: '2rem', borderRadius: '0.75rem' }}>
          <h2 style={{ fontSize: '28px', fontWeight: 'bold', textAlign: 'center', paddingBottom: '0.75rem', paddingTop: '1.25rem' }}>Create Client Account</h2>
          <p style={{ textAlign: 'center', paddingBottom: '0.75rem', paddingTop: '0.25rem', color: '#d1d5db' /* Assuming text-gray-300 */ }}>
            Join FitConnect to find the best professionals for your fitness and wellness needs.
          </p>

          {message && (
            <div
              style={{
                marginTop: '1rem', padding: '0.75rem', borderRadius: '0.5rem', textAlign: 'center',
                color: 'white', backgroundColor: message.includes('successful') ? '#10B981' : '#EF4444', marginBottom: '1rem'
              }}
            >
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ marginTop: '1rem' }}>
            <div style={{ marginBottom: '1.5rem'}}>
              <label htmlFor="name" style={{ display: 'block', fontSize: '1rem', fontWeight: '500', paddingBottom: '0.5rem' }}>Full Name</label>
              <input
                type="text" id="name" name="name" placeholder="Your Full Name"
                onChange={handleChange} value={formData.name} required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div style={{ marginBottom: '1.5rem'}}>
              <label htmlFor="email" style={{ display: 'block', fontSize: '1rem', fontWeight: '500', paddingBottom: '0.5rem' }}>Email</label>
              <input
                type="email" id="email" name="email" placeholder="your.email@example.com"
                onChange={handleChange} value={formData.email} required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div style={{ marginBottom: '1.5rem'}}>
              <label htmlFor="password" style={{ display: 'block', fontSize: '1rem', fontWeight: '500', paddingBottom: '0.5rem' }}>Password</label>
              <input
                type="password" id="password" name="password" placeholder="Choose a strong password"
                onChange={handleChange} value={formData.password} required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div style={{ marginBottom: '1.5rem'}}>
              <label htmlFor="confirmPassword" style={{ display: 'block', fontSize: '1rem', fontWeight: '500', paddingBottom: '0.5rem' }}>Confirm Password</label>
              <input
                type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm your password"
                onChange={handleChange} value={formData.confirmPassword} required
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div style={{ marginBottom: '1.5rem'}}>
              <label htmlFor="phoneNumber" style={{ display: 'block', fontSize: '1rem', fontWeight: '500', paddingBottom: '0.5rem' }}>Phone Number (Optional)</label>
              <input
                type="tel" id="phoneNumber" name="phoneNumber" placeholder="Your Phone Number"
                onChange={handleChange} value={formData.phoneNumber}
                style={{ width: '100%', padding: '1rem', borderRadius: '0.75rem', backgroundColor: '#21284a', border: 'none', color: 'white' }}
              />
            </div>
            <div>
              <button type="submit" style={{ width: '100%', padding: '0.75rem 1.25rem', borderRadius: '9999px', backgroundColor: '#607afb', color: 'white', fontWeight: 'bold', cursor: 'pointer', border: 'none' }}>
                Create Account
              </button>
            </div>
          </form>
          <p style={{ color: '#8e99cc', fontSize: '0.875rem', textAlign: 'center', paddingTop: '1rem' }}>
            Already have an account? <Link to="/login?userType=client" style={{textDecoration:'underline', color: '#8e99cc'}}>Log in</Link>
          </p>
          <p style={{ color: '#8e99cc', fontSize: '0.75rem', textAlign: 'center', paddingTop: '0.5rem' }}>
            By creating an account, you agree to our Terms of Service.
          </p>
        </div>
      </main>
    </div>
  );
};

export default SignupClientPage;
