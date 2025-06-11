import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext'; // Import useAuth

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
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: 'white', color: '#121217', minHeight: '100vh' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #f0f1f4', padding: '0.75rem 2.5rem' }}>
      <Link to="/" style={{textDecoration: 'none', color: '#121217'}}><h2>FitConnect</h2></Link>
        <Link to="/login?userType=professional" style={{ color: '#121217', fontSize: '0.875rem', fontWeight: '500', textDecoration: 'none' }}>Log In</Link>
      </header>
      <main style={{ display: 'flex', justifyContent: 'center', padding: '1.25rem 2.5rem' }}>
        <div style={{ width: '100%', maxWidth: '512px' }}>
          <h2 style={{ fontSize: '32px', fontWeight: 'bold', textAlign: 'center', padding: '1rem' }}>Become a Pro</h2>
          {message && (
            <div style={{ textAlign: 'center', padding: '0.75rem', margin: '1rem 0', borderRadius: '0.5rem', color: 'white', backgroundColor: message.includes('successful') ? '#10B981' : '#EF4444' }}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit} style={{ padding: '0.75rem 1rem', marginTop: '1rem' }}>
            <div style={{ marginBottom: '1rem' }}><label>Full Name: <input type="text" name="name" value={formData.name} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Email: <input type="email" name="email" value={formData.email} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Password: <input type="password" name="password" value={formData.password} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Confirm Password: <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Phone Number: <input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Profession: <input type="text" name="profession" value={formData.profession} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Address: <input type="text" name="address" value={formData.address} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Postal Code: <input type="text" name="postalCode" value={formData.postalCode} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Years of Experience: <input type="number" name="yearsOfExperience" value={formData.yearsOfExperience} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Qualifications: <textarea name="qualifications" value={formData.qualifications} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem', minHeight: '3rem'}} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>About You: <textarea name="aboutYou" value={formData.aboutYou} onChange={handleChange} required style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem', minHeight: '3rem'}} /></label></div>

            <div style={{ marginBottom: '1rem' }}><label>LinkedIn Profile: <input type="url" name="linkedinProfile" value={formData.linkedinProfile} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Website: <input type="url" name="website" value={formData.website} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', border: '1px solid #e2e8f0', borderRadius: '0.375rem' }}/></label></div>

            <div style={{ margin: '1rem 0', padding: '1rem', border: '2px dashed #dcdee5', borderRadius: '0.5rem', textAlign: 'center' }}>
              <p style={{fontWeight: 'bold'}}>Upload Documents</p>
              <p style={{fontSize: '0.875rem'}}>Upload your certifications, licenses, etc.</p>
              <label htmlFor="documents" style={{display: 'inline-block', cursor: 'pointer', backgroundColor: '#f0f1f4', padding: '0.5rem 1rem', borderRadius: '0.5rem', margin: '0.5rem 0', color: '#121317'}}>Choose Files</label>
              <input type="file" id="documents" name="documents" multiple onChange={handleFileChange} style={{display: 'none'}} />
              <div style={{fontSize: '0.875rem', color: '#656a86', marginTop: '0.5rem'}}>{fileListMessage}</div>
            </div>

            <button type="submit" style={{ width: '100%', padding: '0.75rem 1.25rem', borderRadius: '0.5rem', backgroundColor: '#3e58da', color: 'white', fontWeight: 'bold', cursor: 'pointer', border: 'none' }}>
              Submit
            </button>
            <p style={{fontSize: '0.875rem', textAlign: 'center', padding: '0.75rem 0'}}>By submitting, you agree to our Terms of Service and Privacy Policy.</p>
             <p style={{ color: '#656a86', fontSize: '0.875rem', textAlign: 'center', paddingTop: '1rem' }}>
                Already have an account? <Link to="/login?userType=professional" style={{textDecoration:'underline', color: '#656a86'}}>Login</Link>
            </p>
          </form>
        </div>
      </main>
    </div>
  );
};

export default SignupProfessionalPage;
