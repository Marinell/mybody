document.addEventListener('DOMContentLoaded', () => {
    const submitButton = document.getElementById('submitProfessionalRegistration');
    const messageDiv = document.getElementById('professionalRegisterMessage');
    const documentsInput = document.getElementById('documents');
    const fileListDiv = document.getElementById('fileList');

    if (documentsInput && fileListDiv) {
        documentsInput.addEventListener('change', () => {
            fileListDiv.innerHTML = ''; // Clear previous list
            if (documentsInput.files.length > 0) {
                const ul = document.createElement('ul');
                ul.classList.add('list-disc', 'pl-5');
                for (const file of documentsInput.files) {
                    const li = document.createElement('li');
                    li.textContent = file.name;
                    ul.appendChild(li);
                }
                fileListDiv.appendChild(ul);
            } else {
                fileListDiv.textContent = 'No files selected.';
            }
        });
    }

    if (submitButton) {
        submitButton.addEventListener('click', async (event) => {
            event.preventDefault();

            if (messageDiv) {
                messageDiv.textContent = '';
                messageDiv.classList.add('hidden');
                messageDiv.classList.remove('text-green-500', 'text-red-500');
            }

            const formData = new FormData();
            const fields = [
                "name", "email", "password", "phoneNumber", "profession", "address",
                "postalCode", "yearsOfExperience", "qualifications", "aboutYou"
            ];
            let allRequiredFilled = true;
            const requiredFields = ["name", "email", "password", "profession", "qualifications", "aboutYou"];


            fields.forEach(id => {
                const element = document.getElementById(id);
                if (element) {
                    formData.append(id, element.value);
                    if (requiredFields.includes(id) && !element.value.trim()) {
                        allRequiredFilled = false;
                    }
                } else {
                    console.warn(`Element with ID ${id} not found.`);
                }
            });

            if (!allRequiredFilled) {
                if (messageDiv) {
                    messageDiv.textContent = 'Please fill in all required fields (Name, Email, Password, Profession, Qualifications, About You).';
                    messageDiv.classList.remove('hidden');
                    messageDiv.classList.add('text-red-500', 'bg-red-100', 'border', 'border-red-400', 'px-4', 'py-3', 'rounded');
                } else {
                    alert('Please fill in all required fields.');
                }
                return;
            }


            const socialMediaLinks = {
                linkedin: document.getElementById('linkedinProfile')?.value || '',
                instagram: document.getElementById('instagramProfile')?.value || '',
                facebook: document.getElementById('facebookProfile')?.value || '',
                youtube: document.getElementById('youtubeProfile')?.value || '',
                tiktok: document.getElementById('tiktokProfile')?.value || '',
                twitter: document.getElementById('twitterProfile')?.value || '',
                website: document.getElementById('website')?.value || ''
            };
            // Filter out empty links before stringifying
            const filteredSocialLinks = Object.fromEntries(
                Object.entries(socialMediaLinks).filter(([_, value]) => value && value.trim() !== '')
            );
            if (Object.keys(filteredSocialLinks).length > 0) {
                 formData.append('socialMediaLinksJson', JSON.stringify(filteredSocialLinks));
            } else {
                 formData.append('socialMediaLinksJson', JSON.stringify({})); // Send empty JSON object
            }


            if (documentsInput && documentsInput.files.length > 0) {
                for (const file of documentsInput.files) {
                    formData.append('documents', file, file.name);
                }
            } else {
                // No documents attached, which is allowed by backend
                // If documents were required, add validation here.
            }

            // Display loading state on button
            const originalButtonText = submitButton.textContent;
            submitButton.textContent = 'Submitting...';
            submitButton.disabled = true;

            try {
                // apiClient is defined in app.js
                const data = await apiClient('/professionals/register', 'POST', formData, true); // true for FormData
                console.log('Professional registration successful:', data);
                if (messageDiv) {
                    messageDiv.textContent = 'Registration successful! Your profile is under review. You will be redirected to login.';
                    messageDiv.classList.remove('hidden');
                    messageDiv.classList.add('text-green-500', 'bg-green-100', 'border', 'border-green-400', 'px-4', 'py-3', 'rounded');

                }
                // Clear form (optional, as redirecting)
                // document.getElementById('name').form.reset(); // If inputs were in a form tag
                // Manually clear if no form tag
                fields.forEach(id => { if(document.getElementById(id)) document.getElementById(id).value = ''; });
                ['linkedinProfile', 'instagramProfile', 'facebookProfile', 'youtubeProfile', 'tiktokProfile', 'twitterProfile', 'website'].forEach(id => { if(document.getElementById(id)) document.getElementById(id).value = ''; });
                if(documentsInput) documentsInput.value = ''; // Clear file input
                if(fileListDiv) fileListDiv.innerHTML = 'No files selected.';


                setTimeout(() => {
                    redirectTo('frontend/login.html'); // Or professional_profile_verification.html
                }, 3000);

            } catch (error) {
                console.error('Professional registration failed:', error);
                let msg = 'Registration failed. Please try again.';
                 if (error && error.data && typeof error.data === 'string') { // Backend might send plain text error for some validation
                    msg = error.data;
                } else if (error && error.data && error.data.message) {
                    msg = error.data.message;
                } else if (error && error.message) {
                    msg = error.message;
                }
                if (messageDiv) {
                    messageDiv.textContent = msg;
                    messageDiv.classList.remove('hidden');
                    messageDiv.classList.add('text-red-500', 'bg-red-100', 'border', 'border-red-400', 'px-4', 'py-3', 'rounded');
                }
            } finally {
                 submitButton.textContent = originalButtonText;
                 submitButton.disabled = false;
            }
        });
    }
});
