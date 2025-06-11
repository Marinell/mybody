import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../../js/app';
import { useNavigate, Link, useLocation } from 'react-router-dom';

const TopMatchesPage = () => {
  const [matches, setMatches] = useState([]);
  const [rankingCriteria, setRankingCriteria] = useState('');
  const [message, setMessage] = useState('');
  const [pageMessage, setPageMessage] = useState(''); // For general page messages
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedProfessionalId, setSelectedProfessionalId] = useState(null);


  useEffect(() => {
    const currentUser = getUserInfo();
    if (!currentUser || currentUser.role !== 'CLIENT') {
      logout();
      return;
    }

    const queryParams = new URLSearchParams(location.search);
    const serviceRequestId = queryParams.get('serviceRequestId');

    if (!serviceRequestId) {
      setPageMessage('Service Request ID not found in URL. Cannot load matches.');
      return;
    }

    const fetchMatches = async () => {
      setMessage(''); // Clear previous messages
      setPageMessage('');
      try {
        const data = await apiClient(`/service-requests/${serviceRequestId}/matches`);
        setMatches(data.matchedProfessionals || []);
        setRankingCriteria(data.rankingCriteria || "Ranking criteria not provided.");
        if (!data.matchedProfessionals || data.matchedProfessionals.length === 0) {
            setPageMessage('No specific matches found based on your request.');
        }
      } catch (error) {
        setPageMessage(error.data?.message || error.message || 'Failed to fetch top matches.');
        setMatches([]);
        setRankingCriteria('Could not load ranking criteria.');
      }
    };
    fetchMatches();
  }, [navigate, location.search]);

  const handleSelectProfessional = async (professionalId) => {
    setMessage('');
    setSelectedProfessionalId(professionalId); // Mark as attempting to select

    const queryParams = new URLSearchParams(location.search);
    const serviceRequestId = queryParams.get('serviceRequestId');

    try {
        await apiClient(`/service-requests/${serviceRequestId}/select-professional`, 'POST', { professionalId });
        setPageMessage(`Professional selected! They will be notified. You can contact them via their profile.`);
        // Disable all select buttons or change their state
        setMatches(prevMatches => prevMatches.map(m => ({ ...m, selectionAttempted: true })));


    } catch (error) {
        setPageMessage(error.data?.message || error.message || 'Failed to select professional.');
        setSelectedProfessionalId(null); // Clear selection attempt on error
    }
  };

  // Simplified JSX
  return (
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: '#101323', color: 'white', minHeight: '100vh' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #21284a', padding: '0.75rem 2.5rem' }}>
        <Link to="/" style={{textDecoration: 'none', color: 'white'}}><div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}><h2>FitConnect</h2></div></Link>
        {/* Header buttons can be added here if needed */}
        <button onClick={logout} style={{color: 'white', background: 'none', border: 'none', cursor: 'pointer'}}>Logout</button>
      </header>
      <main style={{ display: 'flex', justifyContent: 'center', padding: '1.25rem 2.5rem' }}>
        <div style={{ width: '100%', maxWidth: '960px' }}>
          <h2 style={{ fontSize: '32px', fontWeight: 'bold', padding: '1rem' }}>Top matches for your request</h2>
          {pageMessage && (
            <div style={{ textAlign: 'center', padding: '0.75rem', margin: '1rem', borderRadius: '0.5rem', color: 'white', backgroundColor: pageMessage.includes('failed') || pageMessage.includes('Cannot') ? '#EF4444' : '#10B981' }}>
              {pageMessage}
            </div>
          )}
          <div style={{padding: '1rem'}}>
            <h3 style={{fontSize: '1.125rem', fontWeight: 'bold'}}>Ranking criteria</h3>
            <p style={{fontSize: '0.875rem', color: '#a0aec0'}}>{rankingCriteria}</p>
          </div>

          <h3 style={{fontSize: '1.125rem', fontWeight: 'bold', padding: '1rem 1rem 0.5rem 1rem'}}>Top matches</h3>
          <div style={{display: 'flex', flexDirection: 'column', gap: '0.75rem', padding: '0.5rem 1rem'}}>
            {matches.length === 0 && !pageMessage.includes('failed') && <p>Loading matches or no matches found...</p>}
            {matches.map(prof => (
              <div key={prof.id} style={{ display: 'flex', gap: '1rem', backgroundColor: '#181d35', padding: '1rem', borderRadius: '0.5rem', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{width: '70px', height: '70px', borderRadius: '50%', backgroundColor: '#333'}}> {/* Placeholder for image */}</div>
                <div style={{flexGrow: 1}}>
                  <p style={{fontSize: '1.125rem', fontWeight: '500'}}>{prof.name}</p>
                  <p style={{fontSize: '0.875rem', color: '#a0aec0'}}>{prof.profession} | {prof.yearsOfExperience || 'N/A'} yrs exp.</p>
                  <p style={{fontSize: '0.875rem', color: '#cbd5e0', marginTop: '0.25rem'}}>{prof.summarizedSkills || prof.aboutYouSummary || 'No summary.'}</p>
                  {prof.skills && prof.skills.length > 0 && <p style={{fontSize: '0.75rem', color: '#718096', marginTop: '0.25rem'}}>Skills: {prof.skills.join(', ')}</p>}
                </div>
                <div style={{display: 'flex', flexDirection: 'column', gap: '0.5rem', alignItems: 'flex-end'}}>
                  <Link to={`/professional-profile-view/${prof.id}`} /* Placeholder for actual profile view route */
                        className="flex min-w-[84px] max-w-[150px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-8 px-4 bg-[#21284a] text-white text-xs font-medium leading-normal w-full no-underline">
                        View Profile
                  </Link>
                  <button
                    onClick={() => handleSelectProfessional(prof.id)}
                    disabled={selectedProfessionalId || prof.selectionAttempted} // Disable if any selection is made or attempted
                    style={{ minWidth: '84px', maxWidth: '150px', cursor: 'pointer', height: '2rem', padding: '0 1rem', borderRadius: '9999px', backgroundColor: (selectedProfessionalId || prof.selectionAttempted) ? '#4A5568' : '#607afb', color: 'white', fontSize: '0.75rem', fontWeight: '500', width: '100%', border: 'none'}}
                  >
                    {selectedProfessionalId === prof.id ? 'Selecting...' : (prof.selectionAttempted ? 'Selection Made' : 'Select This Pro')}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
};

export default TopMatchesPage;
