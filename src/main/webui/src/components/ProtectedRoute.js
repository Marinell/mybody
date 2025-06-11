import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Adjust path if context is elsewhere

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isLoggedIn, currentUser, isAuthLoading } = useAuth();
  const location = useLocation();

  if (isAuthLoading) {
    // Optional: Show a loading spinner or a blank screen while auth state is being determined
    return <div>Loading authentication status...</div>;
  }

  if (!isLoggedIn()) {
    // Redirect them to the /login page, but save the current location they were
    // trying to go to. This allows us to send them along to that page after they login,
    // which is a nicer user experience than dropping them off on the home page.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && (!currentUser || !allowedRoles.includes(currentUser.role))) {
    // User is logged in, but their role is not allowed for this route.
    // Redirect to a 'not authorized' page or home page. For now, login.
    // Optionally, you could navigate to a specific '/unauthorized' page.
    alert('You are not authorized to view this page.'); // Simple feedback
    return <Navigate to="/login" replace />; // Or to a more appropriate page like "/"
  }

  return children; // User is authenticated and authorized
};

export default ProtectedRoute;
