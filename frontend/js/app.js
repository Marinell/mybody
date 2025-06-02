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

function logout() {
    removeAuthToken();
    removeUserInfo();
    // Potentially clear other session-related data
    redirectTo('frontend/login.html'); // Assuming login page is at this path from root
}

// --- Navigation ---
function redirectTo(path) {
    // Construct the full path from the repository root
    // window.location.pathname usually gives path from domain root.
    // For local files, this might need adjustment or be relative.
    // Assuming these files are served from a web server where paths are relative to the root.
    // If opening HTML files directly, paths need to be relative from current file.
    // For simplicity, assuming a server context or that paths are handled correctly by browser.
    window.location.href = '../../' + path; // Adjust if files are deeper or served differently
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
            // Token might be expired or invalid
            console.warn('API call returned 401 Unauthorized. Logging out.');
            logout(); // Force logout
            return Promise.reject({ status: 401, message: 'Unauthorized. Please login again.'});
        }
        if (!response.ok) {
            // Try to parse error message from backend if JSON
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
        // If response has no content (e.g., 204 No Content)
        if (response.status === 204) {
            return Promise.resolve(null);
        }
        return response.json(); // Assumes API always returns JSON for OK responses with content
    } catch (error) {
        console.error('Network or other error in apiClient:', error);
        return Promise.reject({ message: error.message || 'Network error or unable to reach API.' });
    }
}

// --- UI Helpers (Placeholders) ---
function displayMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = 'block'; // Make it visible
    } else {
        // Fallback if dedicated message element doesn't exist
        if (isError) console.error(message); else console.log(message);
        // alert(message); // Avoid alerts for better UX in real app
    }
}

function clearMessage(elementId) {
     const element = document.getElementById(elementId);
    if (element) {
        element.textContent = '';
        element.style.display = 'none';
    }
}

// Function to protect pages
function protectPage(allowedRoles = []) { // allowedRoles can be empty (just logged in) or specify roles
    if (!isLoggedIn()) {
        redirectTo('frontend/login.html');
        return false; // Not logged in
    }
    const userInfo = getUserInfo();
    if (userInfo && allowedRoles.length > 0 && !allowedRoles.includes(userInfo.role)) {
        alert('You do not have permission to view this page.'); // Simple alert for now
        // Redirect to a generic dashboard or login
        // For now, let's redirect to login, or a general 'access-denied.html' if we had one.
        logout(); // Or redirect to a more suitable page like a generic dashboard
        return false; // Role not allowed
    }
    return true; // Logged in and role (if specified) is allowed
}


// Example of how to use protectPage at the top of a script for a protected HTML page:
// document.addEventListener('DOMContentLoaded', () => {
//     if (!protectPage(['CLIENT'])) { // Example: only CLIENTs allowed
//         return; // Stop further script execution if redirect happens
//     }
//     // ... rest of the page-specific JavaScript for logged-in, authorized users
// });

console.log('app.js loaded');
