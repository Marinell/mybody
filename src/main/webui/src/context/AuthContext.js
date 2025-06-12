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
        const endpoint = userType === 'professional' ? '/auth/login/professional' : '/auth/login/client';
        try {
            const data = await apiClient(endpoint, 'POST', { email, password });
            if (data.token && data.user) { // Ensure token and user are returned
                storeAuthToken(data.token);
                storeUserInfo(data.user); // data.user should be { id, email, role, name (optional) }
                setToken(data.token);
                setCurrentUser(data.user);

                // Navigate based on role
                if (data.user.role === 'PROFESSIONAL') {
                    navigate('/professional-dashboard');
                } else if (data.user.role === 'CLIENT') {
                    navigate('/customer-request');
                } else {
                    navigate('/'); // Fallback navigation
                }
                return data.user;
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
        isAuthLoading
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
