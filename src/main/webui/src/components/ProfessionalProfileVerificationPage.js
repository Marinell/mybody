import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../../js/app';
import { useNavigate, Link } from 'react-router-dom';

const ProfessionalProfileVerificationPage = () => {
  const [verificationStatus, setVerificationStatus] = useState('');
  const [statusMessage, setStatusMessage] = useState('Loading verification status...');
  const [progressPercent, setProgressPercent] = useState(0);
  const [progressColor, setProgressColor] = useState('#607afb'); // Default blue
  const [actionButton, setActionButton] = useState(null);

  const [certificationFile, setCertificationFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState('');


  const navigate = useNavigate();
  const userInfo = getUserInfo();

  useEffect(() => {
    if (!userInfo || userInfo.role !== 'PROFESSIONAL') {
      logout();
      return;
    }

    const fetchStatus = async () => {
      try {
        const data = await apiClient('/professionals/me/status');
        setVerificationStatus(data.status);
        updateUIBasedOnStatus(data.status);
      } catch (error) {
        setStatusMessage(error.data?.message || error.message || 'Could not retrieve profile status.');
        setProgressPercent(0);
        setProgressColor('red');
        setActionButton({ text: "Try Again", onClick: () => window.location.reload() });
      }
    };
    fetchStatus();
  }, [navigate, userInfo]); // userInfo added as dependency

  const updateUIBasedOnStatus = (status) => {
    switch (status) {
      case 'PENDING_VERIFICATION':
        setStatusMessage("Your profile is currently PENDING VERIFICATION. We'll notify you once it's reviewed.");
        setProgressPercent(50);
        setProgressColor('orange'); // Yellowish-orange for pending
        setActionButton(null);
        break;
      case 'VERIFIED':
        setStatusMessage("Congratulations! Your profile has been VERIFIED.");
        setProgressPercent(100);
        setProgressColor('#607afb'); // Blue for verified
        setActionButton({ text: "Go to My Dashboard", onClick: () => navigate('/professional-dashboard') });
        break;
      case 'REJECTED':
        setStatusMessage("We regret to inform you that your profile submission has been REJECTED. Please check your email for more details or contact support. You may re-upload documents if needed.");
        setProgressPercent(100); // Show full bar but in red
        setProgressColor('red');
        setActionButton({ text: "Contact Support", onClick: () => alert('Please contact support@fitconnect.example.com.') });
        break;
      default:
        setStatusMessage(`Your profile status is: ${status || 'Unknown'}. If you have questions, please contact support.`);
        setProgressPercent(0);
        setProgressColor('gray');
        setActionButton({ text: "Refresh Status", onClick: () => window.location.reload() });
    }
  };

  const handleFileChange = (e) => {
    setCertificationFile(e.target.files[0]);
    setUploadMessage(e.target.files[0] ? e.target.files[0].name : '');
  };

  const handleDocumentSubmit = async (e) => {
    e.preventDefault();
    setUploadMessage('');
    if (!certificationFile) {
      setUploadMessage('Please select a certification file to upload.');
      return;
    }
    const formData = new FormData();
    formData.append('certificationFile', certificationFile); // Assuming backend expects 'certificationFile'
    // If other documents or fields are needed:
    // formData.append('documentType', 'CERTIFICATION');

    try {
      // Endpoint might be different, e.g., '/professionals/me/upload-verification-document'
      await apiClient('/professionals/me/submit-documents', 'POST', formData, true);
      setUploadMessage('Document(s) submitted successfully. Your profile will be reviewed.');
      setCertificationFile(null); // Clear file input
      // Optionally, re-fetch status or navigate
      // fetchStatus();
    } catch (error) {
      setUploadMessage(error.data?.message || error.message || 'Failed to submit document.');
    }
  };

  // Simplified JSX
  return (
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: '#101323', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #21284a', padding: '0.75rem 2.5rem' }}>
      <Link to="/" style={{textDecoration: 'none', color: 'white'}}><div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}><h2>FitConnect</h2></div></Link>
        <button onClick={logout} style={{color: 'white', background: 'none', border: 'none', cursor: 'pointer'}}>Logout</button>
      </header>
      <main style={{ display: 'flex', flex: 1, justifyContent: 'center', alignItems: 'center', padding: '1.25rem' }}>
        <div style={{ width: '100%', maxWidth: '512px', textAlign: 'center' }}>
          <h2 style={{ fontSize: '28px', fontWeight: 'bold', paddingBottom: '0.75rem' }}>Profile Verification</h2>
          <p style={{ paddingBottom: '0.75rem' }}>{statusMessage}</p>

          <div style={{ padding: '1rem 0' }}>
            <p>Verification Progress</p>
            <div style={{ backgroundColor: '#2f396a', borderRadius: '0.25rem', overflow: 'hidden', margin: '0.5rem 0' }}>
              <div style={{ height: '0.5rem', width: `${progressPercent}%`, backgroundColor: progressColor, borderRadius: '0.25rem' }}></div>
            </div>
            <p style={{ fontSize: '0.875rem', color: '#8e99cc' }}>{verificationStatus || "Loading..."}</p>
          </div>

          {actionButton && (
            <div style={{ padding: '0.75rem 0' }}>
              <button
                onClick={actionButton.onClick}
                style={{ padding: '0.5rem 1rem', borderRadius: '9999px', backgroundColor: '#607afb', color: 'white', fontWeight: 'bold', cursor: 'pointer', border: 'none' }}
              >
                {actionButton.text}
              </button>
            </div>
          )}

          { (verificationStatus === 'REJECTED' || verificationStatus === '' || verificationStatus === 'NEEDS_RESUBMISSION') && (
            <form onSubmit={handleDocumentSubmit} style={{marginTop: '2rem', borderTop: '1px solid #21284a', paddingTop: '1.5rem'}}>
                <h3 style={{fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem'}}>Submit/Resubmit Documents</h3>
                <div>
                  <label htmlFor="certificationFile" style={{display: 'block', marginBottom: '0.5rem'}}>Upload Certification/Documents (PDF, JPG, PNG):</label>
                  <input type="file" id="certificationFile" onChange={handleFileChange} accept=".pdf,.jpg,.jpeg,.png" style={{display: 'block', margin: '0 auto 0.5rem auto'}}/>
                  {uploadMessage && <p style={{color: uploadMessage.includes('Failed') ? 'red': 'green', fontSize: '0.875rem'}}>{uploadMessage}</p>}
                </div>
                <button type="submit" disabled={!certificationFile} style={{ padding: '0.5rem 1rem', borderRadius: '9999px', backgroundColor: '#607afb', color: 'white', fontWeight: 'bold', cursor: 'pointer', border: 'none', marginTop: '1rem', opacity: !certificationFile ? 0.5 : 1 }}>
                    Upload Document
                </button>
            </form>
          )}

        </div>
      </main>
    </div>
  );
};

export default ProfessionalProfileVerificationPage;
