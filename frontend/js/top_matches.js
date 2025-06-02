document.addEventListener('DOMContentLoaded', async () => {
    if (!protectPage(['CLIENT'])) {
        return;
    }

    const matchesContainer = document.getElementById('matchesContainer');
    const rankingCriteriaTextEl = document.getElementById('rankingCriteriaText');
    const rankingCriteriaDescriptionEl = document.getElementById('rankingCriteriaDescription'); // Assuming this exists
    const matchPageMessageDiv = document.getElementById('matchPageMessage');

    const urlParams = new URLSearchParams(window.location.search);
    const serviceRequestId = urlParams.get('serviceRequestId');

    function displayMatchMessage(message, isError = false) {
        if (matchPageMessageDiv) {
            matchPageMessageDiv.textContent = message;
            matchPageMessageDiv.className = 'text-center py-3 px-4 text-white'; // Base classes
            if (isError) {
                matchPageMessageDiv.classList.add('bg-red-500');
            } else {
                matchPageMessageDiv.classList.add('bg-green-500');
            }
            matchPageMessageDiv.classList.remove('hidden');
            // Auto-hide after some time for non-errors
            if (!isError) {
                setTimeout(() => { matchPageMessageDiv.classList.add('hidden'); }, 5000);
            }
        } else {
            alert(message);
        }
    }

    if (!serviceRequestId) {
        displayMatchMessage('Service Request ID not found in URL. Cannot load matches.', true);
        if (matchesContainer) matchesContainer.innerHTML = '<p class="text-red-400 text-center">Invalid request.</p>';
        return;
    }

    if (matchesContainer) matchesContainer.innerHTML = '<p class="text-gray-400 text-center">Loading matches...</p>';
    if (rankingCriteriaTextEl) rankingCriteriaTextEl.textContent = 'Loading ranking criteria...';
    if (rankingCriteriaDescriptionEl) rankingCriteriaDescriptionEl.textContent = '';


    try {
        const data = await apiClient(`/service-requests/${serviceRequestId}/matches`, 'GET');

        if (rankingCriteriaTextEl) {
            // The DTO has one 'rankingCriteria' string. We might need to split it or adjust display.
            // For now, putting it all in the main text.
            rankingCriteriaTextEl.textContent = data.rankingCriteria || "Ranking criteria not provided.";
        }
        if (rankingCriteriaDescriptionEl) rankingCriteriaDescriptionEl.textContent = ""; // Clear if not used

        if (matchesContainer) matchesContainer.innerHTML = ''; // Clear loading message

        if (!data.matchedProfessionals || data.matchedProfessionals.length === 0) {
            matchesContainer.innerHTML = '<p class="text-gray-400 text-center py-5">No specific matches found based on your request. You might want to broaden your search criteria or check back later.</p>';
            return;
        }

        data.matchedProfessionals.forEach(pro => {
            const card = document.createElement('div');
            card.className = 'flex gap-4 bg-[#181d35] p-4 rounded-lg shadow-md items-center justify-between text-white'; // Darker card bg

            const imgDiv = document.createElement('div');
            imgDiv.className = 'bg-center bg-no-repeat aspect-square bg-cover rounded-full h-[70px] w-[70px] bg-gray-600 flex-shrink-0';
            // imgDiv.style.backgroundImage = `url('${pro.profileImageUrl || "placeholder.jpg"}')`; // Placeholder

            const textInfoDiv = document.createElement('div');
            textInfoDiv.className = 'flex-grow';
            textInfoDiv.innerHTML = `
                <p class="text-lg font-medium">${pro.name}</p>
                <p class="text-sm text-gray-400">${pro.profession} | ${pro.yearsOfExperience || 'N/A'} yrs exp.</p>
                <p class="text-sm text-gray-300 mt-1 line-clamp-2">${pro.summarizedSkills || pro.aboutYouSummary || 'No summary available.'}</p>
                ${pro.skills && pro.skills.length > 0 ? `<p class="text-xs text-gray-500 mt-1">Skills: ${pro.skills.join(', ')}</p>` : ''}
            `;

            const actionsDiv = document.createElement('div');
            actionsDiv.className = 'flex flex-col gap-2 items-end flex-shrink-0 ml-3';

            const viewProfileBtn = document.createElement('a'); // Changed to <a> for easier navigation
            viewProfileBtn.href = `professional_profile.html?id=${pro.id}`;
            viewProfileBtn.className = 'flex min-w-[84px] max-w-[150px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-8 px-4 bg-[#21284a] text-white text-xs font-medium leading-normal w-full';
            viewProfileBtn.innerHTML = '<span class="truncate">View Profile</span>';

            const selectProBtn = document.createElement('button');
            selectProBtn.className = 'select-pro-btn flex min-w-[84px] max-w-[150px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-8 px-4 bg-[#607afb] text-white text-xs font-medium leading-normal w-full';
            selectProBtn.innerHTML = '<span class="truncate">Select This Pro</span>';
            selectProBtn.dataset.professionalId = pro.id;

            actionsDiv.appendChild(viewProfileBtn);
            actionsDiv.appendChild(selectProBtn);

            card.appendChild(imgDiv);
            card.appendChild(textInfoDiv);
            card.appendChild(actionsDiv);
            matchesContainer.appendChild(card);
        });

        // Add event listener for select buttons (event delegation)
        matchesContainer.addEventListener('click', async (event) => {
            const button = event.target.closest('.select-pro-btn');
            if (button) {
                const professionalId = button.dataset.professionalId;
                button.textContent = 'Selecting...';
                button.disabled = true;

                try {
                    await apiClient(`/service-requests/${serviceRequestId}/select-professional`, 'POST', { professionalId });
                    displayMatchMessage(`Professional selected! They will be notified. You can contact them via their profile.`, false);
                    // Disable all select buttons after one is chosen
                    document.querySelectorAll('.select-pro-btn').forEach(btn => {
                        btn.disabled = true;
                        btn.textContent = 'Selection Made';
                        btn.classList.remove('bg-[#607afb]');
                        btn.classList.add('bg-gray-500');
                    });
                } catch (error) {
                    console.error('Failed to select professional:', error);
                    displayMatchMessage(error.data?.message || error.message || 'Failed to select professional.', true);
                    button.textContent = 'Select This Pro'; // Reset button
                    button.disabled = false;
                }
            }
        });

    } catch (error) {
        console.error('Failed to fetch matches:', error);
        if (matchesContainer) matchesContainer.innerHTML = `<p class="text-red-400 text-center py-5">Error loading matches: ${error.message || 'Please try again later.'}</p>`;
        if (rankingCriteriaTextEl) rankingCriteriaTextEl.textContent = 'Could not load ranking criteria.';
    }
});
