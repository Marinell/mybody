import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Added useNavigate
// import './UserTypeSelectionPage.css'; // If you create a CSS file

const UserTypeSelectionPage = () => {
  const navigate = useNavigate(); // For button clicks if Link is not suitable for styling

  // Tailwind classes used in the original HTML are substantial.
  // For this conversion, I'll simplify the structure and styling.
  // Applying exact Tailwind classes as inline styles or via CSS modules
  // would be a larger effort. This focuses on basic structure and functionality.

  // Replicating SVG directly in JSX can be verbose.
  // It's often better to save SVG as a .svg file and import it as a component or use an <img> tag.
  // For now, I'll omit the SVG for brevity in this step.
  // Simplified FitConnectLogo, assuming app-header-title provides necessary styling
  const FitConnectLogo = () => (
    <Link to="/" className="app-header-title">
      {/* SVG placeholder can be added here if needed */}
      <h2>FitConnect</h2>
    </Link>
  );

  return (
    // Outermost div relies on global body styles for bg, font, text color.
    // Flex column layout is managed by app-header and main-container.
    <div style={{display: 'flex', flexDirection: 'column', minHeight: '100vh'}}>
      <header className="app-header">
        <FitConnectLogo />
        {/* Navigation links can be added here if needed, or keep it minimal */}
      </header>
      <main className="main-container">
        <div className="content-container">
          <h2 className="form-title">Join FitConnect</h2>
          <p className="form-subtext" style={{paddingBottom: '1.5rem', paddingTop: '0.5rem' }}> {/* Adjusted padding for this specific context */}
            Connect with top-tier professionals in sports, fitness, and wellness to achieve your goals.
          </p>
          {/* Buttons container - can use form-group for consistent spacing if desired, or custom div */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%'}}>
            <button
              onClick={() => navigate('/login?userType=professional')}
              className="form-button"
            >
              I'm a Professional
            </button>
            <button
              onClick={() => navigate('/login?userType=client')}
              className="form-button-secondary"
            >
              I'm a Client
            </button>
          </div>
          <p className="form-subtext" style={{marginTop: '1.5rem'}}> {/* Adjusted margin for this specific context */}
            Don't have an account?{' '}
            <Link to="/signup-client">Sign Up as Client</Link> | {' '}
            <Link to="/signup-professional">Sign Up as Professional</Link>
          </p>
        </div>
      </main>
    </div>
  );
};
export default UserTypeSelectionPage;
