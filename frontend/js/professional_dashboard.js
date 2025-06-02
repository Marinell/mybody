document.addEventListener('DOMContentLoaded', async () => {
    if (!protectPage(['PROFESSIONAL'])) {
        return;
    }

    const professionalNameEl = document.getElementById('professionalName');
    const userInfo = getUserInfo();
    if (userInfo && professionalNameEl) {
        professionalNameEl.textContent = userInfo.name || userInfo.email; // Display name or email
    }

    const requestsContainer = document.getElementById('appointmentRequestsContainer');
    if (!requestsContainer) {
        console.error('Error: appointmentRequestsContainer element not found in HTML.');
        return;
    }

    requestsContainer.innerHTML = '<p class="text-center text-gray-500">Loading requests...</p>'; // Loading state

    try {
        const appointmentDTOs = await apiClient('/professionals/me/dashboard', 'GET');

        requestsContainer.innerHTML = ''; // Clear loading message

        if (!appointmentDTOs || appointmentDTOs.length === 0) {
            requestsContainer.innerHTML = '<p class="text-center text-gray-500 py-5">No new appointment requests at this time.</p>';
            return;
        }

        // Grouping (optional, simple list for now)
        // const today = new Date().toLocaleDateString();
        // const requestsToday = [];
        // const requestsUpcoming = [];
        // appointmentDTOs.forEach(req => {
        //     if (new Date(req.createdAt).toLocaleDateString() === today) {
        //         requestsToday.push(req);
        //     } else {
        //         requestsUpcoming.push(req);
        //     }
        // });
        // For now, just a single list of "New Requests"

        const title = document.createElement('h3');
        title.className = 'text-[#121317] text-lg font-bold leading-tight tracking-[-0.015em] px-0 pb-2 pt-4'; // Match existing h3 style
        title.textContent = 'New Client Requests';
        requestsContainer.appendChild(title);

        appointmentDTOs.forEach(req => {
            const requestElement = document.createElement('div');
            requestElement.className = 'flex items-center gap-4 bg-white px-4 py-3 my-2 border border-gray-200 rounded-lg shadow-sm';
            // Added my-2, border, rounded, shadow for better separation

            // You can add a placeholder image or a generic icon
            const iconDiv = document.createElement('div');
            iconDiv.className = 'flex-shrink-0 size-12 bg-gray-200 rounded-full flex items-center justify-center text-gray-500';
            iconDiv.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256"><path d="M230.92,212c-15.23-26.33-38.7-45.21-66.09-54.16a72,72,0,1,0-73.66,0C63.78,166.78,40.31,185.66,25.08,212a8,8,0,1,0,13.85,8c18.84-32.56,52.14-52,89.07-52s70.23,19.44,89.07,52a8,8,0,1,0,13.85-8ZM72,96a56,56,0,1,1,56,56A56.06,56.06,0,0,1,72,96Z"></path></svg>`;


            const infoDiv = document.createElement('div');
            infoDiv.className = 'flex-grow';

            const clientNameP = document.createElement('p');
            clientNameP.className = 'text-[#121317] text-base font-medium leading-normal line-clamp-1';
            clientNameP.textContent = req.clientName || 'N/A';

            const serviceDescP = document.createElement('p');
            serviceDescP.className = 'text-[#686d82] text-sm font-normal leading-normal line-clamp-2';
            serviceDescP.textContent = `Wants: ${req.serviceRequestCategory} - ${req.serviceRequestDescription}`;

            const requestedAtP = document.createElement('p');
            requestedAtP.className = 'text-[#686d82] text-xs font-normal leading-normal';
            requestedAtP.textContent = `Requested: ${new Date(req.createdAt).toLocaleDateString()} ${new Date(req.createdAt).toLocaleTimeString()}`;

            infoDiv.appendChild(clientNameP);
            infoDiv.appendChild(serviceDescP);
            infoDiv.appendChild(requestedAtP);

            const contactDiv = document.createElement('div');
            contactDiv.className = 'flex-shrink-0 ml-4 text-right'; // Added ml-4 for spacing
            contactDiv.innerHTML = `
                <p class="text-sm text-gray-700"><strong>Contact:</strong></p>
                <p class="text-xs text-gray-600">Email: ${req.clientEmail || 'N/A'}</p>
                <p class="text-xs text-gray-600">Phone: ${req.clientPhoneNumber || 'N/A'}</p>
            `;
            // In a real app, you might have "Accept/Decline" buttons here
            // which would make further API calls. For now, just displaying info.


            requestElement.appendChild(iconDiv);
            requestElement.appendChild(infoDiv);
            requestElement.appendChild(contactDiv); // Added contact info display
            requestsContainer.appendChild(requestElement);
        });

    } catch (error) {
        console.error('Failed to fetch professional dashboard data:', error);
        requestsContainer.innerHTML = `<p class="text-center text-red-500 py-5">Error loading requests: ${error.message || 'Please try again later.'}</p>`;
    }
});
