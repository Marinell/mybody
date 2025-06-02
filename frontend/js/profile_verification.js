document.addEventListener('DOMContentLoaded', async () => {
    if (!protectPage(['PROFESSIONAL'])) {
        return; // Stop further script execution if not a logged-in professional
    }

    const statusMessageP = document.getElementById('verificationStatusMessage');
    const progressBarDiv = document.getElementById('verificationProgress');
    const progressTextP = document.getElementById('verificationProgressText');
    const continueButton = document.getElementById('verificationContinueButton'); // This is the original button
    const dashboardLinkContainer = document.getElementById('dashboardLinkContainer');

    // Default state for buttons
    if(continueButton) continueButton.classList.add('hidden');
    if(dashboardLinkContainer) dashboardLinkContainer.classList.add('hidden');


    try {
        const data = await apiClient('/professionals/me/status', 'GET');
        // data is expected to be like { status: "VERIFIED" }

        if (!statusMessageP || !progressBarDiv || !progressTextP || !continueButton || !dashboardLinkContainer) {
            console.error('One or more UI elements for verification status are missing from the HTML.');
            if(statusMessageP) statusMessageP.textContent = 'Error displaying page content. Please contact support.';
            return;
        }

        dashboardLinkContainer.innerHTML = ''; // Clear any previous content

        switch (data.status) {
            case 'PENDING_VERIFICATION':
                statusMessageP.textContent = "Your profile is currently PENDING VERIFICATION. We'll notify you once it's reviewed.";
                progressBarDiv.style.width = "50%";
                progressBarDiv.className = 'h-2 rounded bg-yellow-500'; // Tailwind yellow
                progressTextP.textContent = "Under Review";
                continueButton.classList.add('hidden');
                dashboardLinkContainer.classList.add('hidden');
                break;
            case 'VERIFIED':
                statusMessageP.textContent = "Congratulations! Your profile has been VERIFIED.";
                progressBarDiv.style.width = "100%";
                progressBarDiv.className = 'h-2 rounded bg-[#607afb]'; // Original blue
                progressTextP.textContent = "Verified!";
                continueButton.classList.add('hidden');

                dashboardLinkContainer.classList.remove('hidden');
                dashboardLinkContainer.innerHTML = `
                    <button id="goToDashboardBtn" class="flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-10 px-4 bg-[#607afb] text-white text-sm font-bold leading-normal tracking-[0.015em]">
                        <span class="truncate">Go to My Dashboard</span>
                    </button>`;
                document.getElementById('goToDashboardBtn').addEventListener('click', () => {
                    redirectTo('frontend/professional_dashboard.html');
                });
                break;
            case 'REJECTED':
                statusMessageP.textContent = "We regret to inform you that your profile submission has been REJECTED. Please check your email for more details or contact support.";
                progressBarDiv.style.width = "100%"; // Show full bar but in red
                progressBarDiv.className = 'h-2 rounded bg-red-500'; // Tailwind red
                progressTextP.textContent = "Rejected";

                continueButton.textContent = 'Contact Support';
                continueButton.classList.remove('hidden');
                continueButton.onclick = () => {
                    alert('Please contact support@fitconnect.example.com for assistance regarding your application.');
                };
                dashboardLinkContainer.classList.add('hidden');
                break;
            default:
                statusMessageP.textContent = `Your profile status is: ${data.status}. If you have questions, please contact support.`;
                progressBarDiv.style.width = "0%";
                progressTextP.textContent = "Status Unknown";
                continueButton.classList.remove('hidden');
                continueButton.textContent = "Refresh Status";
                continueButton.onclick = () => { window.location.reload(); };
                dashboardLinkContainer.classList.add('hidden');
        }

    } catch (error) {
        console.error('Failed to fetch profile status:', error);
        if (statusMessageP) {
            statusMessageP.textContent = error.message || 'Could not retrieve profile status. Please try again later.';
        } else {
            alert(error.message || 'Could not retrieve profile status. Please try again later.');
        }
        if(progressBarDiv) progressBarDiv.style.width = "0%";
        if(progressTextP) progressTextP.textContent = "Error";
        if(continueButton) {
            continueButton.classList.remove('hidden');
            continueButton.textContent = "Try Again";
            continueButton.onclick = () => { window.location.reload(); };
        }
    }
});
