document.addEventListener('DOMContentLoaded', async () => {
    if (!protectPage(['CLIENT'])) { // Ensure only CLIENTs can access
        return;
    }

    // Optional: Update user profile picture in header
    const userInfo = getUserInfo();
    // const userProfilePicEl = document.getElementById('userProfilePic'); // Assuming an ID is added to the img/div
    // if (userInfo && userInfo.profileImageUrl && userProfilePicEl) {
    //     userProfilePicEl.style.backgroundImage = `url('${userInfo.profileImageUrl}')`;
    // }


    const categorySelect = document.getElementById('categorySelect');
    const serviceSelect = document.getElementById('serviceSelect'); // May not be used directly in DTO
    const descriptionTextarea = document.getElementById('descriptionTextarea');
    const budgetSelect = document.getElementById('budgetSelect');
    const submitButton = document.getElementById('submitRequestButton');
    const messageDiv = document.getElementById('requestMessageDiv');

    // Populate dropdowns if necessary (e.g., from an API or predefined list)
    // For now, using static options in HTML.

    if (submitButton) {
        submitButton.addEventListener('click', async (event) => {
            event.preventDefault();

            const category = categorySelect.value;
            const service = serviceSelect.value; // Value from the "service" dropdown
            const description = descriptionTextarea.value.trim();
            const budget = budgetSelect.value;

            // Clear previous messages
            if (messageDiv) {
                messageDiv.textContent = '';
                messageDiv.className = 'text-center py-2 px-4 hidden';
            }

            if (!category || category === "one" || !description) {
                if (messageDiv) {
                    messageDiv.textContent = 'Category and description are required.';
                    messageDiv.className = 'text-center py-2 px-4 text-red-400'; // Example error style
                    messageDiv.classList.remove('hidden');
                } else {
                    alert('Category and description are required.');
                }
                return;
            }

            // Constructing description:
            // If service is meaningful and distinct, backend DTO might need an update.
            // For now, prepend service to description if selected and not "one".
            let finalDescription = description;
            if (service && service !== "one") {
                finalDescription = `Service: ${service}. Needs: ${description}`;
            }

            const payload = {
                category: category,
                serviceDescription: finalDescription,
                budget: (budget === "one" ? "" : budget) // Send empty if default "Select budget"
            };

            const originalButtonText = submitButton.textContent;
            submitButton.textContent = 'Submitting...';
            submitButton.disabled = true;

            try {
                const data = await apiClient('/service-requests', 'POST', payload);
                console.log('Service request submitted successfully:', data);
                if (messageDiv) {
                    messageDiv.textContent = 'Request submitted successfully! Finding matches...';
                    messageDiv.className = 'text-center py-2 px-4 text-green-400';
                    messageDiv.classList.remove('hidden');
                }

                // Clear form (optional as redirecting)
                categorySelect.value = "one";
                serviceSelect.value = "one";
                descriptionTextarea.value = "";
                budgetSelect.value = "one";

                // Redirect to top_matches page with the new serviceRequestId
                setTimeout(() => {
                    redirectTo(`frontend/top_matches.html?serviceRequestId=${data.id}`);
                }, 1500);

            } catch (error) {
                console.error('Service request submission failed:', error);
                let msg = 'Submission failed. Please try again.';
                if (error && error.data && error.data.message) {
                    msg = error.data.message;
                } else if (error && error.message) {
                    msg = error.message;
                }
                 if (messageDiv) {
                    messageDiv.textContent = msg;
                    messageDiv.className = 'text-center py-2 px-4 text-red-400';
                    messageDiv.classList.remove('hidden');
                } else {
                    alert(msg);
                }
            } finally {
                submitButton.textContent = originalButtonText;
                submitButton.disabled = false;
            }
        });
    }
});
