import React, { useState, useEffect } from 'react';
import { apiClient } from '../../js/app'; // apiClient is still fine to import directly
import { useAuth } from '../../context/AuthContext'; // Import useAuth
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
            logoutUser();
            return;
        }
        setProfessionalName(currentUser.name || currentUser.email);

        const fetchDashboardData = async () => {
        try {
            const data = await apiClient('/professionals/me/dashboard', 'GET');
            setAppointmentDTOs(data || []);
        } catch (error) {
            setMessage(error.data?.message || error.message || 'Failed to fetch dashboard data.');
            setAppointmentDTOs([]);
        }
        };
        fetchDashboardData();
    }
  }, [currentUser, navigate, isLoggedIn, logoutUser, isAuthLoading]); // Added dependencies

  // Simplified JSX, Tailwind classes omitted.
  // Styles are inline for brevity, consider CSS modules or styled-components for larger app.
  const sidebarStyle = { flexBasis: '320px', /* w-80 */ backgroundColor: 'white', padding: '1rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', minHeight: '700px' };
  const contentStyle = { flex: 1, maxWidth: '960px', padding: '0.5rem 1rem' };
  const navItemStyle = { display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.5rem 0.75rem', borderRadius: '0.75rem', cursor: 'pointer', textDecoration: 'none', color: '#121317' };
  const activeNavItemStyle = { ...navItemStyle, backgroundColor: '#f1f1f4' };

  if (isAuthLoading || (!currentUser && isLoggedIn())) { // Show loading if auth is loading or if logged in but currentUser is not yet set
    return <div>Loading dashboard...</div>;
  }

  if (!currentUser || currentUser.role !== 'PROFESSIONAL') {
    // This case should ideally be handled by the redirect in useEffect,
    // but as a fallback or if navigation is slow:
    return <div>Redirecting to login...</div>;
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: 'white' }}>
      <div style={sidebarStyle}>
        <div>
          <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
            <div style={{ width: '2.5rem', height: '2.5rem', borderRadius: '50%', backgroundColor: '#e0e0e0' }}>{/* Avatar Placeholder */}</div>
            <div>
              <h1 style={{ color: '#121317', fontSize: '1rem', fontWeight: '500' }}>{professionalName}</h1>
              <p style={{ color: '#686d82', fontSize: '0.875rem' }}>Professional</p>
            </div>
          </div>
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <Link to="/professional-dashboard" style={navItemStyle}><HomeIcon /> Home</Link>
            <Link to="/appointment-requests" style={activeNavItemStyle}><TrayIcon /> Requests</Link> {/* Assuming this is active */}
            {/* Placeholder Links for other dashboard sections */}
            <Link to="/professional-profile" style={navItemStyle}><UsersIcon /> Profile</Link>
            <button onClick={logoutUser} style={{ ...navItemStyle, textAlign: 'left', background: 'none', border: 'none', color: '#121317' }}>Logout</button>
          </nav>
        </div>
      </div>

      <div style={contentStyle}>
        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem'}}>
            <div>
                <p style={{color: '#121317', fontSize: '32px', fontWeight: 'bold'}}>Requests</p>
                <p style={{color: '#686d82', fontSize: '14px'}}>Manage your appointment requests and consultations</p>
            </div>
        </div>
        <div style={{borderBottom: '1px solid #dddee4', paddingLeft: '1rem', display: 'flex', gap: '2rem'}}>
            <Link to="/appointment-requests" style={{paddingTop: '1rem', paddingBottom: 'calc(0.875rem - 3px)', borderBottom: '3px solid #121317', color: '#121317', fontWeight: 'bold', fontSize: '14px', textDecoration: 'none'}}>Appointment Requests</Link>
            <Link to="/consultations" style={{paddingTop: '1rem', paddingBottom: 'calc(0.875rem - 3px)', borderBottom: '3px solid transparent', color: '#686d82', fontWeight: 'bold', fontSize: '14px', textDecoration: 'none'}}>Consultations</Link>
        </div>

        {message && <p style={{ color: 'red', textAlign: 'center', padding: '1rem' }}>{message}</p>}

        <div style={{ padding: '0.5rem 1rem' }}>
          <h3 style={{ color: '#121317', fontSize: '1.125rem', fontWeight: 'bold', paddingTop: '1rem', paddingBottom: '0.5rem' }}>New Client Requests</h3>
          {appointmentDTOs.length === 0 && !message && <p style={{ textAlign: 'center', color: '#686d82', padding: '1.25rem 0' }}>No new appointment requests at this time.</p>}
          {appointmentDTOs.map(req => (
            <div key={req.requestId /* Assuming unique ID */} style={{ display: 'flex', alignItems: 'center', gap: '1rem', backgroundColor: 'white', padding: '0.75rem', margin: '0.5rem 0', border: '1px solid #e2e8f0', borderRadius: '0.5rem', boxShadow: '0 1px 2px 0 rgba(0,0,0,0.05)' }}>
              <div style={{ flexShrink: 0, width: '3rem', height: '3rem', borderRadius: '50%', backgroundColor: '#e2e8f0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <ClientIcon />
              </div>
              <div style={{ flexGrow: 1 }}>
                <p style={{ color: '#121317', fontWeight: '500' }}>{req.clientName || 'N/A'}</p>
                <p style={{ color: '#686d82', fontSize: '0.875rem' }}>Wants: {req.serviceRequestCategory} - {req.serviceRequestDescription}</p>
                <p style={{ color: '#686d82', fontSize: '0.75rem' }}>Requested: {new Date(req.createdAt).toLocaleString()}</p>
              </div>
              <div style={{ flexShrink: 0, marginLeft: '1rem', textAlign: 'right' }}>
                <p style={{ fontSize: '0.875rem', color: '#4a5568' }}><strong>Contact:</strong></p>
                <p style={{ fontSize: '0.75rem', color: '#718096' }}>Email: {req.clientEmail || 'N/A'}</p>
                <p style={{ fontSize: '0.75rem', color: '#718096' }}>Phone: {req.clientPhoneNumber || 'N/A'}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProfessionalDashboardPage;
