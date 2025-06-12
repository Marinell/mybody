import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

// Import actual components
import LoginPage from './components/LoginPage';
import SignupClientPage from './components/SignupClientPage';
import UserTypeSelectionPage from './components/UserTypeSelectionPage';
import SignupProfessionalPage from './components/SignupProfessionalPage';
import ProfessionalDashboardPage from './components/ProfessionalDashboardPage';
import CustomerRequestPage from './components/CustomerRequestPage';
import TopMatchesPage from './components/TopMatchesPage';
import ProfessionalProfilePage from './components/ProfessionalProfilePage';
import AppointmentRequestsPage from './components/AppointmentRequestsPage';
import ProfessionalProfileVerificationPage from './components/ProfessionalProfileVerificationPage';
import LogoutButton from './components/LogoutButton';
import ProtectedRoute from './components/ProtectedRoute'; // Import ProtectedRoute


function App() {
  // const basename = process.env.PUBLIC_URL || "/";

  return (
    // <Router basename={basename}>
      <div>
        <nav style={{ backgroundColor: '#333', padding: '1rem' }}>
          <ul style={{ listStyle: 'none', display: 'flex', justifyContent: 'space-around', margin: 0, padding: 0, flexWrap: 'wrap' }}>
            <li style={{ margin: '0.5rem' }}><Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Home</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/login?userType=client" style={{ color: 'white', textDecoration: 'none' }}>Login Client</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/login?userType=professional" style={{ color: 'white', textDecoration: 'none' }}>Login Pro</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/signup-client" style={{ color: 'white', textDecoration: 'none' }}>Signup Client</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/signup-professional" style={{ color: 'white', textDecoration: 'none' }}>Signup Pro</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/professional-dashboard" style={{ color: 'white', textDecoration: 'none' }}>Pro Dashboard</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/customer-request" style={{ color: 'white', textDecoration: 'none' }}>New Request</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/professional-profile" style={{ color: 'white', textDecoration: 'none' }}>Edit Pro Profile</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/appointment-requests" style={{ color: 'white', textDecoration: 'none' }}>Appt. Requests</Link></li>
            <li style={{ margin: '0.5rem' }}><Link to="/professional-profile-verification" style={{ color: 'white', textDecoration: 'none' }}>Verification</Link></li>
            <li style={{ margin: '0.5rem' }}><LogoutButton className="logout-nav-button" /></li>
          </ul>
        </nav>

        <hr />

        <div style={{ padding: '1rem' }}>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup-client" element={<SignupClientPage />} />
            <Route path="/signup-professional" element={<SignupProfessionalPage />} />
            <Route path="/" element={<UserTypeSelectionPage />} />

            {/* Protected Routes */}
            <Route
              path="/professional-dashboard"
              element={
                <ProtectedRoute allowedRoles={['PROFESSIONAL']}>
                  <ProfessionalDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/customer-request"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <CustomerRequestPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/top-matches"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <TopMatchesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/appointment-requests"
              element={
                <ProtectedRoute allowedRoles={['PROFESSIONAL']}>
                  <AppointmentRequestsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/professional-profile"
              element={
                <ProtectedRoute allowedRoles={['PROFESSIONAL']}>
                  <ProfessionalProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/professional-profile-verification"
              element={
                <ProtectedRoute allowedRoles={['PROFESSIONAL']}>
                  <ProfessionalProfileVerificationPage />
                </ProtectedRoute>
              }
            />

            {/* Fallback for unknown routes - optional */}
            {/* <Route path="*" element={<h2>404 Not Found</h2>} /> */}
          </Routes>
        </div>
      </div>
    // </Router>
  );
}

export default App;
