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
        <nav className="app-nav">
          <ul>
            <li><Link to="/">Home</Link></li>
            <li><Link to="/login?userType=client">Login Client</Link></li>
            <li><Link to="/login?userType=professional">Login Pro</Link></li>
            <li><Link to="/signup-client">Signup Client</Link></li>
            <li><Link to="/signup-professional">Signup Pro</Link></li>
            <li><Link to="/professional-dashboard">Pro Dashboard</Link></li>
            <li><Link to="/customer-request">New Request</Link></li>
            <li><Link to="/professional-profile">Edit Pro Profile</Link></li>
            <li><Link to="/appointment-requests">Appt. Requests</Link></li>
            <li><Link to="/professional-profile-verification">Verification</Link></li>
            <li><LogoutButton className="logout-nav-button" /></li>
            {/* Ensure LogoutButton correctly forwards className or uses app-nav button styles internally */}
          </ul>
        </nav>

        {/* <hr /> REMOVED as app-nav should provide separation */}

        <div> {/* Padding removed, global styles should handle this or use a main-container type class if specific padding needed */}
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
