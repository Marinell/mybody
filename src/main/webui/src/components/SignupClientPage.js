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
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}> {/* Global body styles apply font, bg, color */}
      <header className="app-header">
        <Link to="/" className="app-header-title"><div><h2>FitConnect</h2></div></Link>
        <Link to="/login?userType=client">Log In</Link> {/* Global 'a' styles apply, or a specific class if needed */}
      </header>
      <main className="main-container">
        <div className="content-container" style={{maxWidth: '28rem'}}> {/* content-container provides base, inline style for specific max-width */}
          <h2 className="form-title">Create Client Account</h2>
          <p className="form-subtext" style={{paddingBottom: '0.75rem', paddingTop: '0.25rem'}}> {/* form-subtext for base, specific padding overrides */}
            Join FitConnect to find the best professionals for your fitness and wellness needs.
          </p>

          {message && (
            <div className={message.includes('successful') ? 'success-message' : 'error-message'}>
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ marginTop: '1rem' }}> {/* Consider a form class if marginTop is common */}
            <div className="form-group" style={{marginBottom: '1.5rem'}}> {/* form-group for base, specific margin override */}
              <label htmlFor="name" className="form-label">Full Name</label>
              <input
                type="text" id="name" name="name" placeholder="Your Full Name"
                onChange={handleChange} value={formData.name} required
                className="form-input"
              />
            </div>
            <div className="form-group" style={{marginBottom: '1.5rem'}}>
              <label htmlFor="email" className="form-label">Email</label>
              <input
                type="email" id="email" name="email" placeholder="your.email@example.com"
                onChange={handleChange} value={formData.email} required
                className="form-input"
              />
            </div>
            <div className="form-group" style={{marginBottom: '1.5rem'}}>
              <label htmlFor="password" className="form-label">Password</label>
              <input
                type="password" id="password" name="password" placeholder="Choose a strong password"
                onChange={handleChange} value={formData.password} required
                className="form-input"
              />
            </div>
            <div className="form-group" style={{marginBottom: '1.5rem'}}>
              <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
              <input
                type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm your password"
                onChange={handleChange} value={formData.confirmPassword} required
                className="form-input"
              />
            </div>
            <div className="form-group" style={{marginBottom: '1.5rem'}}>
              <label htmlFor="phoneNumber" className="form-label">Phone Number (Optional)</label>
              <input
                type="tel" id="phoneNumber" name="phoneNumber" placeholder="Your Phone Number"
                onChange={handleChange} value={formData.phoneNumber}
                className="form-input"
              />
            </div>
            <div>
              <button type="submit" className="form-button">
                Create Account
              </button>
            </div>
          </form>
          <p className="form-subtext" style={{paddingTop: '1rem'}}>
            Already have an account? <Link to="/login?userType=client">Log in</Link>
          </p>
          <p className="form-subtext" style={{fontSize: '0.75rem', paddingTop: '0.5rem'}}> {/* form-subtext for base, specific font-size and padding */}
            By creating an account, you agree to our Terms of Service.
          </p>
        </div>
      </main>
    </div>
  );
};

export default SignupClientPage;
