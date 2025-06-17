import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../services/app';
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
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}> {/* Rely on global body for theme */}
      <header className="app-header">
        <Link to="/" className="app-header-title"><h2>FitConnect</h2></Link>
        <button onClick={logout} className="logout-nav-button">Logout</button>
      </header>
      <main className="main-container">
        {/* Using content-container but overriding max-width for a wider list area */}
        <div className="content-container" style={{maxWidth: '960px'}}>
          <h2 className="dashboard-title" style={{textAlign: 'left', paddingBottom: '1rem'}}>Top matches for your request</h2>

          {pageMessage && (
            <div className={pageMessage.includes('failed') || pageMessage.includes('Cannot') || pageMessage.includes('not found') ? 'error-message' : 'success-message'} style={{marginBottom: '1rem'}}>
              {pageMessage}
            </div>
          )}

          <div style={{paddingBottom: '1.5rem'}}> {/* Using custom padding for this section */}
            <h3 className="data-list-title" style={{paddingTop: '0', paddingBottom: '0.5rem'}}>Ranking criteria</h3>
            <p className="form-subtext" style={{textAlign: 'left', padding: '0'}}>{rankingCriteria}</p>
          </div>

          <h3 className="data-list-title" style={{paddingTop: '0', paddingBottom: '0.5rem'}}>Top matches</h3>
          <div> {/* Removed flex, flex-direction, gap, padding from this div, relying on match-card margin */}
            {matches.length === 0 && !pageMessage.toLowerCase().includes('failed') &&
              <p className="data-list-empty-message" style={{marginTop:'0'}}>Loading matches or no matches found...</p>
            }
            {matches.map(prof => (
              <div key={prof.id} className="match-card">
                <div className="match-image-placeholder">
                  {/* Display initials or a generic icon if no profileImageUrl */}
                  {prof.profileImageUrl ? <img src={prof.profileImageUrl} alt={prof.name} style={{width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover'}} /> : prof.name?.substring(0,1).toUpperCase()}
                </div>
                <div className="match-details">
                  <p className="match-name">{prof.name}</p>
                  <p className="match-info">{prof.profession} | {prof.yearsOfExperience || 'N/A'} yrs exp.</p>
                  <p className="match-summary">{prof.summarizedSkills || prof.aboutYouSummary || 'No summary available.'}</p>
                  {prof.skills && prof.skills.length > 0 && <p className="match-skills">Skills: {prof.skills.join(', ')}</p>}
                </div>
                <div className="match-actions">
                  <Link to={`/professional-profile-view/${prof.id}`} className="button-outline">
                        View Profile
                  </Link>
                  <button
                    onClick={() => handleSelectProfessional(prof.id)}
                    disabled={selectedProfessionalId || prof.selectionAttempted}
                    className="form-button" style={{fontSize: '0.75rem', padding: '0.5rem 1rem'}} // Smaller font and padding for this context
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
