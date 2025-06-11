import React from 'react';
import { useAuth } from '../context/AuthContext'; // Adjust path if AuthContext is elsewhere

const LogoutButton = ({ className }) => { // Added className prop
  const { logoutUser, currentUser } = useAuth(); // logoutUser from AuthContext

  const handleLogout = () => {
    logoutUser();
    // Navigation is handled by logoutUser in AuthContext
  };

  if (!currentUser) return null; // Don't show if not logged in

  // Apply basic styling or allow className to be passed for flexibility
  const style = className ? {} : {
    background: 'none',
    border: 'none',
    color: 'white', // Default color, can be overridden by className
    cursor: 'pointer',
    padding: '0.5rem 1rem',
    marginLeft: '1rem', // Example spacing if used in a header
    fontSize: '0.875rem',
    fontWeight: '500'
  };


  return (
    <button onClick={handleLogout} className={className} style={!className ? style : undefined}>
      Logout ({currentUser.email})
    </button>
  );
};
export default LogoutButton;
