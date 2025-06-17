import React, { useState, useEffect } from 'react';
import { apiClient } from '../services/app'; // apiClient is still fine to import directly
import { useAuth } from '../context/AuthContext'; // Import useAuth
import { useNavigate, Link } from 'react-router-dom'; // Link for sidebar items

// Placeholder SVGs - in a real app, these would be imported or handled better
const HomeIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M218.83,103.77l-80-75.48a1.14,1.14,0,0,1-.11-.11,16,16,0,0,0-21.53,0l-.11.11L37.17,103.77A16,16,0,0,0,32,115.55V208a16,16,0,0,0,16,16H96a16,16,0,0,0,16-16V160h32v48a16,16,0,0,0,16,16h48a16,16,0,0,0,16-16V115.55A16,16,0,0,0,218.83,103.77ZM208,208H160V160a16,16,0,0,0-16-16H112a16,16,0,0,0-16,16v48H48V115.55l.11-.1L128,40l79.9,75.43.11.1Z"></path></svg>;
const TrayIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M208,32H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32Zm0,176H48V168H76.69L96,187.32A15.89,15.89,0,0,0,107.31,192h41.38A15.86,15.86,0,0,0,160,187.31L179.31,168H208v40Z"></path></svg>;
const CalendarIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32ZM72,48v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24V80H48V48ZM208,208H48V96H208V208Zm-96-88v64a8,8,0,0,1-16,0V132.94l-4.42,2.22a8,8,0,0,1-7.16-14.32l16-8A8,8,0,0,1,112,120Zm59.16,30.45L152,176h16a8,8,0,0,1,0,16H136a8,8,0,0,1-6.4-12.8l28.78-38.37A8,8,0,1,0,145.07,132a8,8,0,1,1-13.85-8A24,24,0,0,1,176,136,23.76,23.76,0,0,1,171.16,150.45Z"></path></svg>;
const UsersIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M117.25,157.92a60,60,0,1,0-66.5,0A95.83,95.83,0,0,0,3.53,195.63a8,8,0,1,0,13.4,8.74,80,80,0,0,1,134.14,0,8,8,0,0,0,13.4-8.74A95.83,95.83,0,0,0,117.25,157.92ZM40,108a44,44,0,1,1,44,44A44.05,44.05,0,0,1,40,108Zm210.14,98.7a8,8,0,0,1-11.07-2.33A79.83,79.83,0,0,0,172,168a8,8,0,0,1,0-16,44,44,0,1,0-16.34-84.87,8,8,0,1,1-5.94-14.85,60,60,0,0,1,55.53,105.64,95.83,95.83,0,0,1,47.22,37.71A8,8,0,0,1,250.14,206.7Z"></path></svg>;
const PaymentsIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M152,120H136V56h8a32,32,0,0,1,32,32,8,8,0,0,0,16,0,48.05,48.05,0,0,0-48-48h-8V24a8,8,0,0,0-16,0V40h-8a48,48,0,0,0,0,96h8v64H104a32,32,0,0,1-32-32,8,8,0,0,0-16,0,48.05,48.05,0,0,0,48,48h16v16a8,8,0,0,0,16,0V216h16a48,48,0,0,0,0-96Zm-40,0a32,32,0,0,1,0-64h8v64Zm40,80H136V136h16a32,32,0,0,1,0,64Z"></path></svg>;
const ClientIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M230.92,212c-15.23-26.33-38.7-45.21-66.09-54.16a72,72,0,1,0-73.66,0C63.78,166.78,40.31,185.66,25.08,212a8,8,0,1,0,13.85,8c18.84-32.56,52.14-52,89.07-52s70.23,19.44,89.07,52a8,8,0,1,0,13.85-8ZM72,96a56,56,0,1,1,56,56A56.06,56.06,0,0,1,72,96Z"></path></svg>;


const ProfessionalDashboardPage = () => {
  const [professionalName, setProfessionalName] = useState('');
  const [appointmentDTOs, setAppointmentDTOs] = useState([]);
  const [message, setMessage] = useState('');
  const { currentUser, logoutUser, isLoggedIn, isAuthLoading } = useAuth();
  const navigate = useNavigate();


  useEffect(() => {
    if (!isAuthLoading) { // Only run when auth state is resolved
        if (!isLoggedIn() || !currentUser || currentUser.role !== 'PROFESSIONAL') {
            logoutUser(); // This will trigger redirect via AuthContext's ProtectedRoute logic or similar
            return;
        }
        setProfessionalName(currentUser.name || currentUser.email);

        const fetchDashboardData = async () => {
        try {
            // Assuming apiClient is set up to handle authorization tokens
            const data = await apiClient('/professionals/me/dashboard', 'GET');
            setAppointmentDTOs(data || []);
        } catch (error) {
            setMessage(error.data?.message || error.message || 'Failed to fetch dashboard data.');
            setAppointmentDTOs([]); // Ensure it's an array even on error
        }
        };
        fetchDashboardData();
    }
  }, [currentUser, navigate, isLoggedIn, logoutUser, isAuthLoading]);

  // Use a path map or similar for active link detection if more links are added
  const currentPath = window.location.pathname;

  if (isAuthLoading || (!currentUser && isLoggedIn())) {
    return <div className="form-subtext" style={{textAlign: 'center', paddingTop: '2rem'}}>Loading dashboard...</div>;
  }

  if (!currentUser || currentUser.role !== 'PROFESSIONAL') {
    return <div className="form-subtext" style={{textAlign: 'center', paddingTop: '2rem'}}>Redirecting to login...</div>;
  }

  return (
    <div className="dashboard-layout">
      <aside className="dashboard-sidebar">
        <div>
          <div className="dashboard-sidebar-header">
            <div className="sidebar-avatar-placeholder">
              {/* Can use initials or a generic icon */}
              {professionalName.substring(0,1).toUpperCase()}
            </div>
            <div>
              <h1 className="sidebar-professional-name">{professionalName}</h1>
              <p className="sidebar-professional-role">Professional</p>
            </div>
          </div>
          <nav className="dashboard-nav">
            <Link to="/professional-dashboard" className={`dashboard-nav-item ${currentPath === '/professional-dashboard' ? 'active' : ''}`}><HomeIcon /> Home</Link>
            <Link to="/appointment-requests" className={`dashboard-nav-item ${currentPath === '/appointment-requests' ? 'active' : ''}`}><TrayIcon /> Requests</Link>
            <Link to="/professional-profile" className={`dashboard-nav-item ${currentPath === '/professional-profile' ? 'active' : ''}`}><UsersIcon /> Profile</Link>
            {/* Add more links here with appropriate icons and active class logic */}
          </nav>
        </div>
        <nav className="dashboard-nav"> {/* Second nav for items at the bottom like logout */}
             <button onClick={logoutUser} className="dashboard-nav-item dashboard-logout-button">Logout</button>
        </nav>
      </aside>

      <main className="dashboard-content">
        <header className="dashboard-content-header">
            <div>
                <h2 className="dashboard-title">Requests</h2>
                <p className="dashboard-subtitle">Manage your appointment requests and consultations</p>
            </div>
            {/* Potential actions like "New Request" button could go here */}
        </header>
        <nav className="dashboard-tabs">
            {/* Assuming /appointment-requests is the default view for this page or a sub-route */}
            <Link to="/appointment-requests" className="dashboard-tab-item active">Appointment Requests</Link>
            <Link to="/consultations" className="dashboard-tab-item">Consultations</Link> {/* Example for another tab */}
        </nav>

        {message && !message.includes("success") && <div className="error-message">{message}</div>}
        {message && message.includes("success") && <div className="success-message">{message}</div>}


        <div> {/* Content for the active tab */}
          <h3 className="data-list-title">New Client Requests</h3>
          {appointmentDTOs.length === 0 && !message.includes("Failed") && (
            <p className="data-list-empty-message">No new appointment requests at this time.</p>
          )}
          {appointmentDTOs.map(req => (
            <div key={req.requestId || req.id /* Use a unique ID from your DTO */} className="appointment-card">
              <div className="appointment-client-icon-container">
                <ClientIcon /> {/* Ensure SVG is styled to be visible */}
              </div>
              <div className="appointment-details">
                <p className="appointment-client-name">{req.clientName || 'N/A'}</p>
                <p className="appointment-service-info">
                  Wants: {req.serviceRequestCategory || 'N/A'} - {req.serviceRequestDescription || 'No description provided.'}
                </p>
                <p className="appointment-timestamp">Requested: {new Date(req.createdAt).toLocaleString()}</p>
              </div>
              <div className="appointment-contact-info">
                <p className="appointment-contact-title">Contact:</p>
                <p className="appointment-contact-detail">Email: {req.clientEmail || 'N/A'}</p>
                <p className="appointment-contact-detail">Phone: {req.clientPhoneNumber || 'N/A'}</p>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
};

export default ProfessionalDashboardPage;
