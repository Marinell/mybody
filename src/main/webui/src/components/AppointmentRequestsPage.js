import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../services/app';
import { useNavigate, Link } from 'react-router-dom';

// Placeholder SVGs - in a real app, these would be imported or handled better
const HomeIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M218.83,103.77l-80-75.48a1.14,1.14,0,0,1-.11-.11,16,16,0,0,0-21.53,0l-.11.11L37.17,103.77A16,16,0,0,0,32,115.55V208a16,16,0,0,0,16,16H96a16,16,0,0,0,16-16V160h32v48a16,16,0,0,0,16,16h48a16,16,0,0,0,16-16V115.55A16,16,0,0,0,218.83,103.77ZM208,208H160V160a16,16,0,0,0-16-16H112a16,16,0,0,0-16,16v48H48V115.55l.11-.1L128,40l79.9,75.43.11.1Z"></path></svg>;
const TrayIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M208,32H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32Zm0,176H48V168H76.69L96,187.32A15.89,15.89,0,0,0,107.31,192h41.38A15.86,15.86,0,0,0,160,187.31L179.31,168H208v40Z"></path></svg>;
const CalendarIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M208,32H184V24a8,8,0,0,0-16,0v8H88V24a8,8,0,0,0-16,0v8H48A16,16,0,0,0,32,48V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V48A16,16,0,0,0,208,32ZM112,184a8,8,0,0,1-16,0V132.94l-4.42,2.22a8,8,0,0,1-7.16-14.32l16-8A8,8,0,0,1,112,120Zm56-8a8,8,0,0,1,0,16H136a8,8,0,0,1-6.4-12.8l28.78-38.37A8,8,0,1,0,145.07,132a8,8,0,1,1-13.85-8A24,24,0,0,1,176,136,23.76,23.76,0,0,1-4.84,14.45L152,176ZM48,80V48H72v8a8,8,0,0,0,16,0V48h80v8a8,8,0,0,0,16,0V48h24V80Z"></path></svg>;
const UsersIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M117.25,157.92a60,60,0,1,0-66.5,0A95.83,95.83,0,0,0,3.53,195.63a8,8,0,1,0,13.4,8.74,80,80,0,0,1,134.14,0,8,8,0,0,0,13.4-8.74A95.83,95.83,0,0,0,117.25,157.92ZM40,108a44,44,0,1,1,44,44A44.05,44.05,0,0,1,40,108Zm210.14,98.7a8,8,0,0,1-11.07-2.33A79.83,79.83,0,0,0,172,168a8,8,0,0,1,0-16,44,44,0,1,0-16.34-84.87,8,8,0,1,1-5.94-14.85,60,60,0,0,1,55.53,105.64,95.83,95.83,0,0,1,47.22,37.71A8,8,0,0,1,250.14,206.7Z"></path></svg>;
const PaymentsIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M152,120H136V56h8a32,32,0,0,1,32,32,8,8,0,0,0,16,0,48.05,48.05,0,0,0-48-48h-8V24a8,8,0,0,0-16,0V40h-8a48,48,0,0,0,0,96h8v64H104a32,32,0,0,1-32-32,8,8,0,0,0-16,0,48.05,48.05,0,0,0,48,48h16v16a8,8,0,0,0,16,0V216h16a48,48,0,0,0,0-96Zm-40,0a32,32,0,0,1,0-64h8v64Zm40,80H136V136h16a32,32,0,0,1,0,64Z"></path></svg>;
const ClientIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M230.92,212c-15.23-26.33-38.7-45.21-66.09-54.16a72,72,0,1,0-73.66,0C63.78,166.78,40.31,185.66,25.08,212a8,8,0,1,0,13.85,8c18.84-32.56,52.14-52,89.07-52s70.23,19.44,89.07,52a8,8,0,1,0,13.85-8ZM72,96a56,56,0,1,1,56,56A56.06,56.06,0,0,1,72,96Z"></path></svg>;


const AppointmentRequestsPage = () => {
  const [requests, setRequests] = useState([]);
  const [message, setMessage] = useState('');
  const [professionalName, setProfessionalName] = useState('');
  const navigate = useNavigate();
  const userInfo = getUserInfo();

  useEffect(() => {
    if (!userInfo || userInfo.role !== 'PROFESSIONAL') {
      logout();
      return;
    }
    setProfessionalName(userInfo.name || userInfo.email);

    const fetchRequests = async () => {
      setMessage('');
      try {
        // Endpoint might be different, e.g. /professionals/me/appointments?status=PENDING
        const data = await apiClient('/professionals/me/appointment-requests');
        setRequests(data || []);
        if (!data || data.length === 0) {
            setMessage('No new appointment requests.');
        }
      } catch (error) {
        setMessage(error.data?.message || error.message || 'Failed to fetch appointment requests.');
        setRequests([]);
      }
    };
    fetchRequests();
  }, [navigate, userInfo]);

  const handleUpdateRequest = async (requestId, status) => {
    setMessage('');
    try {
      await apiClient(`/professionals/me/appointment-requests/${requestId}`, 'PUT', { status });
      setMessage(`Request ${requestId} has been ${status.toLowerCase()}.`);
      // Refresh list: filter out declined, or update status for confirmed
      setRequests(prev =>
        prev.map(r => r.id === requestId ? {...r, status: status} : r)
            .filter(r => status === 'DECLINED' ? r.id !== requestId : true)
      );
    } catch (error) {
      setMessage(error.data?.message || error.message || `Failed to update request ${requestId}.`);
    }
  };

  // Simplified JSX structure
  const sidebarStyle = { flexBasis: '320px', backgroundColor: 'white', padding: '1rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', minHeight: '700px' };
  const contentStyle = { flex: 1, maxWidth: '960px', padding: '0.5rem 1rem' };
  const navItemStyle = { display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.5rem 0.75rem', borderRadius: '0.75rem', cursor: 'pointer', textDecoration: 'none', color: '#121317' };
  const activeNavItemStyle = { ...navItemStyle, backgroundColor: '#f1f1f4' };


  if (!userInfo || userInfo.role !== 'PROFESSIONAL') {
    return <div>Loading or redirecting...</div>;
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: 'white' }}>
      <div style={sidebarStyle}>
        <div>
          <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
            <div style={{ width: '2.5rem', height: '2.5rem', borderRadius: '50%', backgroundColor: '#e0e0e0' }}>{/* Avatar */}</div>
            <div>
              <h1 style={{ color: '#121317', fontSize: '1rem', fontWeight: '500' }}>{professionalName}</h1>
              <p style={{ color: '#656a86', fontSize: '0.875rem' }}>Professional</p>
            </div>
          </div>
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <Link to="/professional-dashboard" style={navItemStyle}><HomeIcon /> Home</Link>
            <Link to="/appointment-requests" style={activeNavItemStyle}><TrayIcon /> Requests</Link>
            <Link to="/consultations" style={navItemStyle}><CalendarIcon /> Consultations</Link>
            <Link to="/clients" style={navItemStyle}><UsersIcon /> Clients</Link>
            <Link to="/payments" style={navItemStyle}><PaymentsIcon /> Payments</Link>
            <button onClick={logout} style={{ ...navItemStyle, textAlign: 'left', background: 'none', border: 'none' }}>Logout</button>
          </nav>
        </div>
      </div>

      <div style={contentStyle}>
        <h2 style={{ fontSize: '32px', fontWeight: 'bold', padding: '1rem' }}>Appointment Requests</h2>
        {message && <p style={{ color: message.includes('Failed') ? 'red' : 'green', textAlign: 'center', padding: '1rem' }}>{message}</p>}

        {requests.length === 0 && !message.includes('Failed') && <p style={{textAlign: 'center', padding: '1rem'}}>No appointment requests at this time.</p>}

        {requests.map(req => (
          <div key={req.id} style={{ display: 'flex', alignItems: 'center', gap: '1rem', backgroundColor: 'white', padding: '0.75rem', margin: '0.5rem 0', border: '1px solid #e2e8f0', borderRadius: '0.5rem' }}>
            <div style={{ flexShrink: 0, width: '3.5rem', height: '3.5rem', borderRadius: '50%', backgroundColor: '#e2e8f0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ClientIcon />
            </div>
            <div style={{ flexGrow: 1 }}>
              <p style={{ color: '#121317', fontWeight: '500' }}>{req.clientName || `Client ID: ${req.clientId}`}</p>
              <p style={{ color: '#656a86', fontSize: '0.875rem' }}>Service: {req.serviceType}</p>
              <p style={{ color: '#656a86', fontSize: '0.875rem' }}>Preferred Date: {new Date(req.preferredDateTime).toLocaleString()}</p>
              <p style={{ color: '#656a86', fontSize: '0.875rem', marginTop: '0.25rem' }}>Description: {req.description}</p>
              <p style={{ color: '#656a86', fontSize: '0.875rem' }}>Status: {req.status}</p>
            </div>
            {req.status === 'PENDING' && (
              <div style={{ flexShrink: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <button onClick={() => handleUpdateRequest(req.id, 'CONFIRMED')} style={{padding: '0.25rem 0.75rem', backgroundColor: 'green', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer'}}>Approve</button>
                <button onClick={() => handleUpdateRequest(req.id, 'DECLINED')} style={{padding: '0.25rem 0.75rem', backgroundColor: 'red', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer'}}>Decline</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default AppointmentRequestsPage;
