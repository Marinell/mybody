import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Import useAuth

const SignupProfessionalPage = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    profession: '',
    address: '',
    postalCode: '',
    yearsOfExperience: '',
    qualifications: '',
    aboutYou: '',
    linkedinProfile: '',
    instagramProfile: '',
    facebookProfile: '',
    youtubeProfile: '',
    tiktokProfile: '',
    twitterProfile: '',
    website: ''
  });
  const [documents, setDocuments] = useState([]);
  const [message, setMessage] = useState('');
  const [fileListMessage, setFileListMessage] = useState('No files selected.');
  const { signup } = useAuth(); // Get signup from context
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e) => {
    setDocuments(Array.from(e.target.files));
    if (e.target.files.length > 0) {
      setFileListMessage(Array.from(e.target.files).map(f => f.name).join(', '));
    } else {
      setFileListMessage('No files selected.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    const requiredFields = ["name", "email", "password", "profession", "qualifications", "aboutYou", "yearsOfExperience", "address", "postalCode"];
    for (const field of requiredFields) {
        if (!formData[field] || (typeof formData[field] === 'string' && !formData[field].trim())) { // Check for empty strings too
            setMessage(`Please fill in all required fields. Missing: ${field}`);
            return;
        }
    }

    if (formData.password !== formData.confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    const payload = new FormData(); // For multipart/form-data

    // Append all text fields from formData (excluding confirmPassword)
    for (const key in formData) {
      if (key !== 'confirmPassword') {
        payload.append(key, formData[key]);
      }
    }

    // Append social media links as a JSON string
    const socialMediaLinks = {
        linkedin: formData.linkedinProfile,
        instagram: formData.instagramProfile,
        facebook: formData.facebookProfile,
        youtube: formData.youtubeProfile,
        tiktok: formData.tiktokProfile,
        twitter: formData.twitterProfile,
        website: formData.website
    };
    const filteredSocialLinks = Object.fromEntries(
        Object.entries(socialMediaLinks).filter(([_, value]) => value && value.trim() !== '')
    );
    payload.append('socialMediaLinksJson', JSON.stringify(Object.keys(filteredSocialLinks).length > 0 ? filteredSocialLinks : {}));

    // Append documents
    for (const file of documents) {
      payload.append('documents', file, file.name);
    }

    try {
      await signup(payload, 'professional', true); // Use signup from context, true for FormData
      setMessage('Professional signup successful! Your profile is under review. Redirecting to login...');
      setTimeout(() => navigate('/login?userType=professional'), 3000);
    } catch (error) {
      setMessage(error.data?.message || error.data || error.message || 'Signup failed.');
    }
  };

  // Simplified JSX structure, Tailwind classes omitted for brevity.
  // Styles are inline for consistency.
  return (
    // Main div uses global body styles for background, font, text color. Flex column layout.
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <header className="app-header">
        <Link to="/" className="app-header-title"><h2>FitConnect</h2></Link>
        <Link to="/login?userType=professional">Log In</Link>
      </header>
      <main className="main-container">
        <div className="content-container"> {/* Max width is 512px by default from content-container */}
          <h2 className="form-title">Become a Pro</h2>
          {message && (
            <div className={message.includes('successful') ? 'success-message' : 'error-message'}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit}> {/* Removed inline padding and margin */}
            {/* Using label-flex-container for each form field */}
            <div className="label-flex-container">
              <label htmlFor="name">Full Name:</label>
              <input type="text" id="name" name="name" value={formData.name} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="email">Email:</label>
              <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="password">Password:</label>
              <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="confirmPassword">Confirm Password:</label>
              <input type="password" id="confirmPassword" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="phoneNumber">Phone Number:</label>
              <input type="tel" id="phoneNumber" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="profession">Profession:</label>
              <input type="text" id="profession" name="profession" value={formData.profession} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="address">Address:</label>
              <input type="text" id="address" name="address" value={formData.address} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="postalCode">Postal Code:</label>
              <input type="text" id="postalCode" name="postalCode" value={formData.postalCode} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="yearsOfExperience">Years of Experience:</label>
              <input type="number" id="yearsOfExperience" name="yearsOfExperience" value={formData.yearsOfExperience} onChange={handleChange} required className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="qualifications">Qualifications:</label>
              <textarea id="qualifications" name="qualifications" value={formData.qualifications} onChange={handleChange} required className="form-textarea" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="aboutYou">About You:</label>
              <textarea id="aboutYou" name="aboutYou" value={formData.aboutYou} onChange={handleChange} required className="form-textarea" />
            </div>

            <h3 className="form-title" style={{fontSize: '1.25rem', paddingTop: '1.5rem', paddingBottom: '1rem'}}>Social Links (Optional)</h3>
            <div className="label-flex-container">
              <label htmlFor="linkedinProfile">LinkedIn Profile:</label>
              <input type="url" id="linkedinProfile" name="linkedinProfile" value={formData.linkedinProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="instagramProfile">Instagram Profile:</label>
              <input type="url" id="instagramProfile" name="instagramProfile" value={formData.instagramProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="facebookProfile">Facebook Profile:</label>
              <input type="url" id="facebookProfile" name="facebookProfile" value={formData.facebookProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="youtubeProfile">YouTube Profile:</label>
              <input type="url" id="youtubeProfile" name="youtubeProfile" value={formData.youtubeProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="tiktokProfile">TikTok Profile:</label>
              <input type="url" id="tiktokProfile" name="tiktokProfile" value={formData.tiktokProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="twitterProfile">Twitter Profile:</label>
              <input type="url" id="twitterProfile" name="twitterProfile" value={formData.twitterProfile} onChange={handleChange} className="form-input" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="website">Website:</label>
              <input type="url" id="website" name="website" value={formData.website} onChange={handleChange} className="form-input" />
            </div>

            <div className="file-upload-container">
              <p style={{fontWeight: 'bold', color: 'white'}}>Upload Documents</p> {/* Adjusted text color */}
              <p className="form-subtext" style={{paddingTop: '0', paddingBottom: '0.5rem'}}>Upload your certifications, licenses, etc.</p>
              <label htmlFor="documents" className="file-upload-label">Choose Files</label>
              <input type="file" id="documents" name="documents" multiple onChange={handleFileChange} style={{display: 'none'}} />
              <div className="file-upload-list">{fileListMessage}</div>
            </div>

            <button type="submit" className="form-button">
              Submit Application
            </button>
            <p className="form-subtext" style={{fontSize: '0.875rem', paddingTop: '0.75rem', paddingBottom: '0.75rem'}}>By submitting, you agree to our Terms of Service and Privacy Policy.</p>
            <p className="form-subtext">
                Already have an account? <Link to="/login?userType=professional">Login</Link>
            </p>
          </form>
        </div>
      </main>
    </div>
  );
};

export default SignupProfessionalPage;
