import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    apiClient,
    getAuthToken,
    getUserInfo as getStoredUserInfo, // Renamed to avoid conflict with component state
    saveAuthToken as storeAuthToken,   // Renamed for clarity
    saveUserInfo as storeUserInfo,     // Renamed for clarity
    removeAuthToken as removeStoredAuthToken, // Renamed for clarity
    removeUserInfo as removeStoredUserInfo,   // Renamed for clarity
    isLoggedIn as checkIsLoggedIn, // Direct use from app.js
    logout as baseLogout // The logout from app.js that clears storage
} from '../services/app'; // Path to your app.js utilities

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [currentUser, setCurrentUser] = useState(null); // Initialize with null
    const [token, setToken] = useState(null); // Initialize with null
    const [isAuthLoading, setIsAuthLoading] = useState(true);
    const navigate = useNavigate();

    const updateUserProfileStatus = (newStatus) => {
      setCurrentUser(prevUser => {
        if (!prevUser) return null; // Should not happen if a user is logged in
        const updatedUser = { ...prevUser, profileStatus: newStatus };
        storeUserInfo(updatedUser); // storeUserInfo is the renamed saveUserInfo
        return updatedUser;
      });
    };

    useEffect(() => {
        // Initial check on component mount
        const storedUser = getStoredUserInfo();
        const storedToken = getAuthToken();
        if (storedUser && storedToken) {
            setCurrentUser(storedUser);
            setToken(storedToken);
        }
        setIsAuthLoading(false);
    }, []);

    const login = async (email, password, userType = 'client') => {
        // const endpoint = userType === 'professional' ? '/auth/login/professional' : '/auth/login/client';
        const endpoint ='/auth/login';
        try {
            const data = await apiClient(endpoint, 'POST', { email, password });
            if (data.token) { // Ensure token
                storeAuthToken(data.token);
                // Add profileStatus to the user object
                var user = {"email": data.email, "role": data.role, "userId": data.userId, "profileStatus": data.profileStatus};
                storeUserInfo(user);
                setToken(data.token);
                setCurrentUser(user); // This will now include profileStatus

                // Navigate based on role and profileStatus
                if (user.role === 'PROFESSIONAL') {
                    if (user.profileStatus === 'PENDING_VERIFICATION') {
                        navigate('/professional-profile-verification');
                    } else if (user.profileStatus === 'VERIFIED') {
                        navigate('/professional-dashboard');
                    } else {
                        // Fallback for PROFESSIONAL if status is unexpected (e.g., REJECTED or null/undefined)
                        // Redirect to login or a generic error page, or verification page as a safe default
                        navigate('/professional-profile-verification');
                        // Or consider navigate('/login'); if REJECTED means they shouldn't access anything
                    }
                } else if (user.role === 'CLIENT') {
                    navigate('/customer-request');
                } else {
                    navigate('/'); // Fallback navigation for other roles or if role is undefined
                }
                return user;
            } else {
                throw new Error(data.message || "Login failed: No token or user info returned.");
            }
        } catch (error) {
            // Re-throw the error so the component can catch it
            throw error;
        }
    };

    const signup = async (userData, userType = 'client', isFormData = false) => {
        const endpoint = userType === 'professional' ? '/professionals/register' : '/auth/register/client';
         try {
            // For professional signup, userData is FormData, for client it's JSON
            await apiClient(endpoint, 'POST', userData, isFormData);
            // After signup, typically navigate to login or show a success message
            // This context function doesn't handle navigation directly, components will.
        } catch (error) {
            throw error;
        }
    };

    const logoutUser = () => {
        baseLogout(); // Call app.js logout to clear localStorage
        setToken(null);
        setCurrentUser(null);
        navigate('/login'); // Redirect to login after logout
    };

    const value = {
        currentUser,
        token,
        isLoggedIn: checkIsLoggedIn,
        login,
        signup,
        logout: logoutUser,
        isAuthLoading,
        updateUserProfileStatus // Add the new function here
    };

    // Render children only after initial auth state is determined (optional, but good for UX)
    if (isAuthLoading) {
        return <div>Loading authentication...</div>; // Or a spinner component
    }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
