document.addEventListener('DOMContentLoaded', async () => {
    // Allow any logged-in user to view profiles for now
    // If only clients, change to: if (!protectPage(['CLIENT'])) return;
    if (!protectPage([])) {
        return;
    }

    const profilePageMessageDiv = document.getElementById('profilePageMessage');

    // Helper to display messages on this page
    function displayProfileMessage(message, isError = false) {
        if (profilePageMessageDiv) {
            profilePageMessageDiv.textContent = message;
            profilePageMessageDiv.className = 'text-center py-3 px-4 text-white'; // Base classes
            if (isError) {
                profilePageMessageDiv.classList.add('bg-red-500');
            } else {
                // No specific success background for general info load
            }
            profilePageMessageDiv.classList.remove('hidden');
        } else {
            alert(message);
        }
    }

    const urlParams = new URLSearchParams(window.location.search);
    const professionalId = urlParams.get('id');

    if (!professionalId) {
        displayProfileMessage('Professional ID not found in URL. Cannot load profile.', true);
        return;
    }

    // Get references to all elements that will display data
    const profileImageEl = document.getElementById('profileImage'); // For background image
    const profileNameEl = document.getElementById('profileName');
    const profileProfessionEl = document.getElementById('profileProfession');
    const profileLocationEl = document.getElementById('profileLocation'); // May not be used if data.address is full
    const profileEmailEl = document.getElementById('profileEmail');
    const profilePhoneNumberEl = document.getElementById('profilePhoneNumber');
    const contactButton = document.getElementById('contactButton'); // May become redundant if info shown directly

    const profileAboutTextEl = document.getElementById('profileAboutText');
    const profileSkillsContainerEl = document.getElementById('profileSkillsContainer');
    const socialLinksContainerEl = document.getElementById('socialLinksContainer');

    // Set loading states
    if (profileNameEl) profileNameEl.textContent = 'Loading...';
    if (profileProfessionEl) profileProfessionEl.textContent = 'Loading...';
    if (profileAboutTextEl) profileAboutTextEl.textContent = 'Loading about information...';
    if (profileSkillsContainerEl) profileSkillsContainerEl.innerHTML = '<p class="text-gray-400">Loading skills...</p>';
    if (socialLinksContainerEl) socialLinksContainerEl.innerHTML = '<p class="text-gray-400">Loading links...</p>';
    if (profileEmailEl) profileEmailEl.textContent = 'Loading...';
    if (profilePhoneNumberEl) profilePhoneNumberEl.textContent = 'Loading...';


    try {
        const data = await apiClient(`/professionals/${professionalId}/client-view`, 'GET');
        console.log("Professional Profile Data:", data);

        if (profileImageEl && data.profileImageUrl) { // Assuming DTO might have profileImageUrl
            profileImageEl.style.backgroundImage = `url('${data.profileImageUrl}')`;
        } else if (profileImageEl) {
            // Keep placeholder or set a default generic one
            profileImageEl.style.backgroundImage = `url('https://via.placeholder.com/128/cccccc/808080?text=Pro')`; // Placeholder
        }

        if (profileNameEl) profileNameEl.textContent = data.name || 'N/A';
        if (profileProfessionEl) profileProfessionEl.textContent = data.profession || 'N/A';

        // Location: DTO has full address. We might want to display city or just hide if too long.
        // For now, let's assume data.address contains what's needed or it's part of aboutYou.
        if (profileLocationEl && data.address) { // If we decide to use address for location
             profileLocationEl.textContent = data.address; // Or parse city from it
        } else if (profileLocationEl) {
            profileLocationEl.textContent = ''; // Or hide
        }

        if (profileEmailEl) profileEmailEl.textContent = data.email || 'Not Provided';
        if (profilePhoneNumberEl) profilePhoneNumberEl.textContent = data.phoneNumber || 'Not Provided';

        // Contact button functionality (e.g., mailto, tel links)
        if (contactButton) {
            contactButton.addEventListener('click', () => {
                // Example: open mail client. Or show a modal with contact info.
                // For now, info is displayed directly. Button can be for emphasis or future use.
                if(data.email) window.location.href = `mailto:${data.email}`;
                else if(data.phoneNumber) window.location.href = `tel:${data.phoneNumber}`;
                else alert("Contact information is displayed on the page.");
            });
        }


        if (profileAboutTextEl) profileAboutTextEl.textContent = data.aboutYou || 'No detailed about information provided.';

        if (profileSkillsContainerEl) {
            profileSkillsContainerEl.innerHTML = ''; // Clear loading/static
            if (data.skills && data.skills.length > 0) {
                data.skills.forEach(skill => {
                    const skillBadge = document.createElement('div');
                    skillBadge.className = 'flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-xl bg-[#f1f1f4] pl-4 pr-4';
                    skillBadge.innerHTML = `<p class="text-[#121317] text-sm font-medium leading-normal">${skill}</p>`;
                    profileSkillsContainerEl.appendChild(skillBadge);
                });
            } else {
                profileSkillsContainerEl.innerHTML = '<p class="text-gray-500">No specific skills listed.</p>';
            }
        }

        if (socialLinksContainerEl) {
            socialLinksContainerEl.innerHTML = ''; // Clear loading/static
            if (data.socialMediaLinks && Object.keys(data.socialMediaLinks).length > 0) {
                for (const [platform, url] of Object.entries(data.socialMediaLinks)) {
                    if (url) { // Ensure URL is not empty
                        const linkElement = document.createElement('a');
                        linkElement.href = url.startsWith('http') ? url : `https://${url}`;
                        linkElement.target = "_blank"; // Open in new tab
                        linkElement.rel = "noopener noreferrer";
                        linkElement.className = "text-[#607afb] hover:underline text-sm block py-1";
                        linkElement.textContent = `${platform.charAt(0).toUpperCase() + platform.slice(1)} Profile`;
                        socialLinksContainerEl.appendChild(linkElement);
                    }
                }
            } else {
                socialLinksContainerEl.innerHTML = '<p class="text-gray-500">No social or web links provided.</p>';
            }
        }
        if (profilePageMessageDiv) profilePageMessageDiv.classList.add('hidden'); // Hide if previously shown for error

    } catch (error) {
        console.error('Failed to fetch professional profile:', error);
        displayProfileMessage(error.data?.message || error.message || 'Could not load professional profile.', true);
        // Hide or clear other elements if load fails
        const mainContent = document.querySelector('.layout-content-container.flex.flex-col.max-w-\[960px\].flex-1');
        if(mainContent) {
            // Example: find the "About" H2 and hide everything after if profile fails to load.
            // This part can be more robust based on actual structure if needed.
        }
    }
});
