// FitConnect App - app.js

const API_BASE_URL = 'http://localhost:8080/api'; // Assuming backend runs on 8080

// --- Authentication Token Management ---
function saveAuthToken(token) {
    localStorage.setItem('fitconnect_auth_token', token);
}

function getAuthToken() {
    return localStorage.getItem('fitconnect_auth_token');
}

function removeAuthToken() {
    localStorage.removeItem('fitconnect_auth_token');
}

function saveUserInfo(userInfo) {
    // userInfo could be an object like { id: 1, email: 'user@example.com', role: 'CLIENT' }
    localStorage.setItem('fitconnect_user_info', JSON.stringify(userInfo));
}

function getUserInfo() {
    const userInfoStr = localStorage.getItem('fitconnect_user_info');
    return userInfoStr ? JSON.parse(userInfoStr) : null;
}

function removeUserInfo() {
    localStorage.removeItem('fitconnect_user_info');
}

function isLoggedIn() {
    return !!getAuthToken();
}

// Modified logout: Navigation will be handled by React Router via AuthContext
function logout() {
    removeAuthToken();
    removeUserInfo();
    // Potentially clear other session-related data
    // redirectTo('frontend/login.html'); // Removed: Navigation handled by calling component/context
    console.log("User logged out, token and user info removed from local storage.");
}

// --- Navigation ---
// This redirectTo is for legacy HTML pages. React components will use react-router-dom.
function redirectTo(path) {
    window.location.href = '../../' + path;
}

// --- API Client ---
async function apiClient(endpoint, method = 'GET', body = null, isFormData = false) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = getAuthToken();
    const headers = {};

    if (!isFormData) {
        headers['Content-Type'] = 'application/json';
    }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method: method,
        headers: headers,
    };

    if (body) {
        config.body = isFormData ? body : JSON.stringify(body);
    }

    try {
        const response = await fetch(url, config);
        if (response.status === 401) { // Unauthorized
            console.warn('API call returned 401 Unauthorized. Logging out.');
            // The original logout() here caused a redirect, which might not be desired for all API calls.
            // For React, it's better to let the calling code (e.g., AuthContext) handle navigation.
            removeAuthToken(); // Clear token to force re-login on next protected action
            removeUserInfo();
            return Promise.reject({ status: 401, message: 'Unauthorized. Please login again.'});
        }
        if (!response.ok) {
            let errorData;
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                errorData = await response.json();
            } else {
                errorData = { message: await response.text() || `HTTP error! status: ${response.status}`};
            }
            console.error('API Error:', response.status, errorData);
            return Promise.reject({ status: response.status, data: errorData, message: errorData.message || `HTTP error! status: ${response.status}` });
        }
        if (response.status === 204) {
            return Promise.resolve(null);
        }
        return response.json();
    } catch (error) {
        console.error('Network or other error in apiClient:', error);
        return Promise.reject({ message: error.message || 'Network error or unable to reach API.' });
    }
}

// --- UI Helpers (Deprecated for React components but kept for any remaining legacy JS) ---
function displayMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = 'block';
    } else {
        if (isError) console.error(message); else console.log(message);
    }
}

function clearMessage(elementId) {
     const element = document.getElementById(elementId);
    if (element) {
        element.textContent = '';
        element.style.display = 'none';
    }
}

// Function to protect pages (Deprecated for React components)
function protectPage(allowedRoles = []) {
    if (!isLoggedIn()) {
        redirectTo('frontend/login.html');
        return false;
    }
    const userInfo = getUserInfo();
    if (userInfo && allowedRoles.length > 0 && !allowedRoles.includes(userInfo.role)) {
        alert('You do not have permission to view this page.');
        logout(); // Original logout called redirectTo
        return false;
    }
    return true;
}

console.log('app.js loaded (refactored for React context usage)');

// Removed global logout button event listener

// Ensure these are available for import
export {
    API_BASE_URL,
    saveAuthToken,
    getAuthToken,
    removeAuthToken,
    saveUserInfo,
    getUserInfo,
    removeUserInfo,
    isLoggedIn,
    logout, // To be called by AuthContext
    apiClient,
    redirectTo, // Kept for any legacy JS that might still use it
    displayMessage, // Kept for legacy
    clearMessage,   // Kept for legacy
    protectPage     // Kept for legacy
};
