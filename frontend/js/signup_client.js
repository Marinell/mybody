document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('clientSignupForm');
    const messageDiv = document.getElementById('clientRegisterMessage'); // Using the new message div

    if (signupForm) {
        signupForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value.trim();
            const phoneNumber = document.getElementById('phoneNumber').value.trim();

            // Clear previous messages
            if (messageDiv) {
                messageDiv.textContent = '';
                messageDiv.className = 'text-center py-2 hidden'; // Reset classes and hide
            }

            if (!name || !email || !password) {
                 if (messageDiv) {
                    messageDiv.textContent = 'Name, email, and password are required.';
                    messageDiv.className = 'text-center py-2 error'; // Show with error class
                    messageDiv.classList.remove('hidden');
                } else {
                    alert('Name, email, and password are required.');
                }
                return;
            }

            const payload = { name, email, password };
            if (phoneNumber) {
                payload.phoneNumber = phoneNumber;
            }

            try {
                const data = await apiClient('/auth/register/client', 'POST', payload);
                console.log('Client registration successful:', data);
                if (messageDiv) {
                    messageDiv.textContent = 'Registration successful! Please proceed to login.';
                    messageDiv.className = 'text-center py-2 success'; // Show with success class
                    messageDiv.classList.remove('hidden');
                } else {
                    alert('Registration successful! Please log in.');
                }
                signupForm.reset(); // Clear the form
                // Optional: Redirect to login after a short delay
                setTimeout(() => {
                    redirectTo('frontend/login.html');
                }, 2000);

            } catch (error) {
                console.error('Client registration failed:', error);
                let msg = 'Registration failed. Please try again.';
                if (error && error.data && error.data.message) {
                    msg = error.data.message;
                } else if (error && error.message) {
                    msg = error.message;
                }
                if (messageDiv) {
                    messageDiv.textContent = msg;
                    messageDiv.className = 'text-center py-2 error';
                    messageDiv.classList.remove('hidden');
                } else {
                    alert(msg);
                }
            }
        });
    }
});
