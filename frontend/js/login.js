document.addEventListener('DOMContentLoaded', () => {
    const loginButton = document.getElementById('loginButton');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const errorMessageDiv = document.getElementById('loginErrorMessage');

    // Check if already logged in, if so, redirect appropriately
    if (isLoggedIn()) {
        const userInfo = getUserInfo();
        if (userInfo) {
            if (userInfo.role === 'PROFESSIONAL') {
                redirectTo('frontend/professional_dashboard.html');
            } else if (userInfo.role === 'CLIENT') {
                redirectTo('frontend/customer_request.html');
            } else {
                // Default redirect or specific admin page
                console.log('Logged in as non-standard role or admin, no specific redirect yet.');
            }
        }
    }


    if (loginButton) {
        loginButton.addEventListener('click', async (event) => {
            event.preventDefault(); // Prevent default form submission if it were a form

            const email = emailInput.value.trim();
            const password = passwordInput.value.trim();

            // Clear previous messages
            if (errorMessageDiv) {
                 errorMessageDiv.textContent = '';
                 errorMessageDiv.classList.add('hidden'); // Tailwind class to hide
            }

            if (!email || !password) {
                if (errorMessageDiv) {
                    errorMessageDiv.textContent = 'Email and password are required.';
                    errorMessageDiv.classList.remove('hidden');
                } else {
                    alert('Email and password are required.');
                }
                return;
            }

            try {
                const data = await apiClient('/auth/login', 'POST', { email, password });
                console.log('Login successful:', data);
                saveAuthToken(data.token);
                saveUserInfo({
                    id: data.userId,
                    email: data.email,
                    role: data.role
                });

                // Redirect based on role
                if (data.role === 'PROFESSIONAL') {
                    redirectTo('frontend/professional_dashboard.html');
                } else if (data.role === 'CLIENT') {
                    redirectTo('frontend/customer_request.html');
                } else if (data.role === 'ADMIN') {
                    // Example: redirectTo('frontend/admin_dashboard.html');
                    alert('Admin login successful. Admin dashboard not yet implemented.');
                    console.log('Admin logged in, redirect to admin dashboard (not implemented).');
                } else {
                    alert('Login successful, but role unclear or no dashboard defined.');
                    console.log('Logged in with role:', data.role);
                }

            } catch (error) {
            debugger;
                console.error('Login failed:', error);
                let message = 'Login failed. Please check your credentials and try again.';
                if (error && error.data && error.data.message) {
                    message = error.data.message;
                } else if (error && error.message) {
                    message = error.message;
                }

                if (errorMessageDiv) {
                    errorMessageDiv.textContent = message;
                    errorMessageDiv.classList.remove('hidden');
                } else {
                    alert(message);
                }
            }
        });
    } else {
        console.error('Login button not found. Ensure the button has id="loginButton".');
    }

    // Add event listener for "Don't have an account? Sign up"
    const signupLink = document.getElementById('signup');
    if (signupLink) {
        signupLink.addEventListener('click', (e) => {
            e.preventDefault();
            redirectTo('frontend/user_type_selection_page.html');
        });
        signupLink.style.cursor = 'pointer'; // Make it look clickable
    }


});
