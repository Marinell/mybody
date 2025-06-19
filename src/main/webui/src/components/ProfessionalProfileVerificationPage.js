import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../services/app';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Add this

const ProfessionalProfileVerificationPage = () => {
  const [verificationStatus, setVerificationStatus] = useState('');
  const [statusMessage, setStatusMessage] = useState('Loading verification status...');
  const [progressPercent, setProgressPercent] = useState(0);
  const [progressColor, setProgressColor] = useState('#607afb'); // Default blue
  const [actionButton, setActionButton] = useState(null);
  const [statusIcon, setStatusIcon] = useState(null); // New state for SVG icon

  const [certificationFile, setCertificationFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState('');


  const navigate = useNavigate();
  const userInfo = getUserInfo();
  const { updateUserProfileStatus } = useAuth(); // Destructure the new function

// SVG Icon Definitions
const PENDING_ICON = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="status-icon pending-icon"><path d="M12 2C6.486 2 2 6.486 2 12s4.486 10 10 10 10-4.486 10-10S17.514 2 12 2zm0 18c-4.411 0-8-3.589-8-8s3.589-8 8-8 8 3.589 8 8-3.589 8-8 8z"></path><path d="M13 7h-2v5.414l3.293 3.293 1.414-1.414L13 10.586V7z"></path></svg>';
const VERIFIED_ICON = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="status-icon verified-icon"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"></path></svg>';
const REJECTED_ICON = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="status-icon rejected-icon"><path d="M12 2C6.486 2 2 6.486 2 12s4.486 10 10 10 10-4.486 10-10S17.514 2 12 2zm4.207 12.793-1.414 1.414L12 13.414l-2.793 2.793-1.414-1.414L10.586 12 7.793 9.207l1.414-1.414L12 10.586l2.793-2.793 1.414 1.414L13.414 12l2.793 2.793z"></path></svg>';
const DEFAULT_ICON = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="status-icon default-icon"><path d="M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"></path></svg>';


  useEffect(() => {
    if (!userInfo || userInfo.role !== 'PROFESSIONAL') {
      logout();
      return;
    }

    const fetchStatus = async () => {
      try {
        const data = await apiClient('/professionals/me/status');
        setVerificationStatus(data.status); // Local state update
        if (updateUserProfileStatus) { // Ensure the function is available
            updateUserProfileStatus(data.status); // Global auth context update
        }
        updateUIBasedOnStatus(data.status);
      } catch (error) {
        setStatusMessage(error.data?.message || error.message || 'Could not retrieve profile status.');
        setProgressPercent(0);
        setProgressColor('red');
        // Using outline button for "Try Again" as it's a refresh-like action
        setActionButton({ text: "Try Again", onClick: () => window.location.reload(), styleClass: "button-outline" });
      }
    };
    fetchStatus();
  }, [navigate, userInfo?.role]); // userInfo added as dependency

  const updateUIBasedOnStatus = (status) => {
    switch (status) {
      case 'PENDING_VERIFICATION':
        setStatusMessage("Your profile is currently PENDING VERIFICATION. We'll notify you once it's reviewed.");
        setProgressPercent(50);
        setProgressColor('orange'); // Yellowish-orange for pending
        setStatusIcon(PENDING_ICON);
        setActionButton(null);
        break;
      case 'VERIFIED':
        setStatusMessage("Congratulations! Your profile has been VERIFIED.");
        setProgressPercent(100);
        setProgressColor('#607afb'); // Blue for verified
        setStatusIcon(VERIFIED_ICON);
        setActionButton({ text: "Go to My Dashboard", onClick: () => navigate('/professional-dashboard') });
        break;
      case 'REJECTED':
        setStatusMessage("We regret to inform you that your profile submission has been REJECTED. Please check your email for more details or contact support. You may re-upload documents if needed.");
        setProgressPercent(100); // Show full bar but in red
        setProgressColor('red');
        setStatusIcon(REJECTED_ICON);
        setActionButton({ text: "Contact Support", onClick: () => alert('Please contact support@fitconnect.example.com.') });
        break;
      default:
        setStatusMessage(`Your profile status is: ${status || 'Unknown'}. If you have questions, please contact support.`);
        setProgressPercent(0);
        setProgressColor('gray');
        setStatusIcon(DEFAULT_ICON); // Using a default info icon
        // Using outline button for "Refresh Status"
        setActionButton({ text: "Refresh Status", onClick: () => window.location.reload(), styleClass: "button-outline" });
    }
  };

  const handleFileChange = (e) => {
    setCertificationFile(e.target.files[0]);
    setUploadMessage(e.target.files[0] ? e.target.files[0].name : '');
  };

  const handleDocumentSubmit = async (e) => {
    e.preventDefault();
    // Clear previous file name message before new status message, unless it's an error for not selecting a file
    if (certificationFile) {
        setUploadMessage('');
    }
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
    // Main page container styles (font, background, color, minHeight) are handled by body tag in global.css
    // display: flex, flexDirection: column are for overall page structure, can be kept or handled by a wrapper class if needed
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      <header className="app-header">
      <Link to="/" className="app-header-title"><div><h2>FitConnect</h2></div></Link>
        <button onClick={logout} className="logout-nav-button">Logout</button>
      </header>
      <main className="main-container">
        {/* Removed text-center from content-container, will apply to specific children */}
        <div className="content-container">
          {/* form-title already includes text-align: center */}
          <h2 className="form-title">Profile Verification</h2>
          {/* Added pb-3 and text-center for padding-bottom: 0.75rem and centering. Added icon via dangerouslySetInnerHTML */}
          <p className="pb-3 text-center">
            {statusIcon && <span className="status-icon-wrapper" dangerouslySetInnerHTML={{ __html: statusIcon }} />}
            {statusMessage}
          </p>

          {/* Added py-4 for padding: 1rem 0 */}
          <div className="py-4 text-center"> {/* Added text-center here for this section */}
            {/* Added mb-2 for margin-bottom: 0.5rem */}
            <p className="mb-2">Verification Progress</p>
            {/* Added progress-bar-background class */}
            <div className="progress-bar-background" style={{ margin: '0.5rem 0' }}> {/* margin kept inline for now or make part of class */}
              {/* Added progress-bar-indicator class, dynamic styles width and backgroundColor remain inline */}
              <div className="progress-bar-indicator" style={{ width: `${progressPercent}%`, backgroundColor: progressColor }}></div>
            </div>
            {/* form-subtext already includes text-align: center */}
            <p className="form-subtext">{verificationStatus || "Loading..."}</p>
          </div>

          {actionButton && (
            // Added py-3 for padding: 0.75rem 0
            <div className="py-3 text-center"> {/* Added text-center here for this section */}
              <button
                onClick={actionButton.onClick}
                // Use actionButton.styleClass if provided, otherwise default to "form-button"
                className={actionButton.styleClass || "form-button"}
              >
                {actionButton.text}
              </button>
            </div>
          )}

          { (verificationStatus === 'REJECTED' || verificationStatus === '' || verificationStatus === 'NEEDS_RESUBMISSION') && (
            // Replaced custom margin/padding classes with standardized ones (mt-8, pt-6). border-t-custom remains.
            <form onSubmit={handleDocumentSubmit} className="mt-8 pt-6 border-t-custom">
                {/* Added utility classes for font size, weight, and margin. These may need to be created in global.css */}
                <h3 className="text-xl font-bold mb-4">Submit/Resubmit Documents</h3>
                <div>
                  <label htmlFor="certificationFile" className="form-label">Upload Certification/Documents (PDF, JPG, PNG):</label>
                  {/* Visually hidden input, new label acts as button */}
                  <input
                    type="file"
                    id="certificationFile"
                    onChange={handleFileChange}
                    accept=".pdf,.jpg,.jpeg,.png"
                    className="visually-hidden"
                  />
                  <label htmlFor="certificationFile" className="file-upload-label d-block mx-auto mb-2" style={{width: 'fit-content'}}>Choose File</label>

                  {uploadMessage &&
                    (() => {
                      let messageClass = 'fs-small'; // Base class
                      const lowerUploadMessage = uploadMessage.toLowerCase();
                      if (lowerUploadMessage.includes('failed') || lowerUploadMessage.startsWith('please select')) {
                        messageClass += ' error-message-text';
                      } else if (lowerUploadMessage.includes('successfully') || lowerUploadMessage.includes('submitted')) {
                        messageClass += ' success-message-text';
                      } else if (certificationFile && uploadMessage === certificationFile.name) {
                        // This condition means it's displaying the selected file name
                        messageClass += ' selected-file-text';
                      }
                      // Default to subtle color if no other condition met but message exists
                      if (messageClass === 'fs-small') messageClass += ' selected-file-text';

                      return <p className={messageClass}>{uploadMessage}</p>;
                    })()
                  }
                </div>
                {/* Added mt-4 for margin-top. Opacity remains inline. Button itself is centered via form-button styles. */}
                <button type="submit" disabled={!certificationFile} className="form-button mt-4" style={{ opacity: !certificationFile ? 0.5 : 1 }}>
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
