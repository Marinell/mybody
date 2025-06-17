import React, { useState, useEffect } from 'react';
import { apiClient, getUserInfo, logout } from '../services/app'; // Adjusted path
import { useNavigate, Link } from 'react-router-dom'; // Added Link

const ProfessionalProfilePage = () => {
  const [profileData, setProfileData] = useState({
    name: '', // From original professional_signup.html, assuming 'name' is full name
    email: '',
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
    website: '',
    profileImageUrl: '', // Added for profile image
    skills: [] // Assuming skills are an array of strings
  });
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const userInfo = getUserInfo();

  useEffect(() => {
    if (!userInfo || userInfo.role !== 'PROFESSIONAL') {
      logout();
      return;
    }

    const fetchProfile = async () => {
      setIsLoading(true);
      try {
        const data = await apiClient('/professionals/me'); // Endpoint to get own profile
        // data.socialMediaLinks is expected to be a JSON string, parse it
        let socialLinks = {};
        if (data.socialMediaLinksJson && typeof data.socialMediaLinksJson === 'string') {
            try {
                socialLinks = JSON.parse(data.socialMediaLinksJson);
            } catch (e) {
                console.error("Error parsing socialMediaLinksJson: ", e);
            }
        } else if (data.socialMediaLinks && typeof data.socialMediaLinks === 'object') {
            // If it's already an object (less likely based on signup script)
            socialLinks = data.socialMediaLinks;
        }

        setProfileData({
          name: data.name || '',
          email: data.email || '', // Email might not be editable, but good to have
          phoneNumber: data.phoneNumber || '',
          profession: data.profession || '',
          address: data.address || '',
          postalCode: data.postalCode || '',
          yearsOfExperience: data.yearsOfExperience || '',
          qualifications: data.qualifications || '',
          aboutYou: data.aboutYou || '',
          profileImageUrl: data.profileImageUrl || '',
          skills: data.skills || [],
          linkedinProfile: socialLinks.linkedin || '',
          instagramProfile: socialLinks.instagram || '',
          facebookProfile: socialLinks.facebook || '',
          youtubeProfile: socialLinks.youtube || '',
          tiktokProfile: socialLinks.tiktok || '',
          twitterProfile: socialLinks.twitter || '',
          website: socialLinks.website || ''
        });
        setMessage('');
      } catch (error) {
        setMessage('Failed to load profile. ' + (error.data?.message || error.message));
      }
      setIsLoading(false);
    };
    fetchProfile();
  }, []); // Removed userInfo from dep array as it causes re-fetch on state change

  const handleChange = (e) => {
    setProfileData({ ...profileData, [e.target.name]: e.target.value });
  };

  const handleSkillsChange = (e) => {
    // Assuming skills are comma-separated string in input, then converted to array
    const skillsArray = e.target.value.split(',').map(skill => skill.trim()).filter(skill => skill);
    setProfileData({ ...profileData, skills: skillsArray });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      // Prepare payload, especially socialMediaLinksJson
      const {linkedinProfile, instagramProfile, facebookProfile, youtubeProfile, tiktokProfile, twitterProfile, website, ...otherData} = profileData;
      const socialMediaLinks = {
          linkedin: linkedinProfile, instagram: instagramProfile, facebook: facebookProfile,
          youtube: youtubeProfile, tiktok: tiktokProfile, twitter: twitterProfile, website: website
      };
      const filteredSocialLinks = Object.fromEntries(
          Object.entries(socialMediaLinks).filter(([_, value]) => value && value.trim() !== '')
      );
      const payload = {
          ...otherData,
          socialMediaLinksJson: JSON.stringify(Object.keys(filteredSocialLinks).length > 0 ? filteredSocialLinks : {})
      };

      await apiClient('/professionals/me', 'PUT', payload); // Endpoint to update own profile
      setMessage('Profile updated successfully!');
    } catch (error) {
      setMessage(error.data?.message || error.message || 'Failed to update profile.');
    }
  };

  if (isLoading) return <p className="form-subtext" style={{textAlign: 'center', paddingTop: '2rem'}}>Loading profile...</p>; // Using form-subtext for themed loading message

  // Simplified JSX structure for an editable form
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}> {/* Global body styles for theme, flex for layout */}
      <header className="app-header">
        <Link to="/professional-dashboard" className="app-header-title"><h2>FitConnect Dashboard</h2></Link>
        <button onClick={logout} className="logout-nav-button">Logout</button>
      </header>
      <main className="main-container">
        {/* content-container sets max-width to 512px, this page might need wider for better form layout.
            Override content-container's max-width with an inline style or a new class if needed.
            For now, we'll use content-container as is, which might make the form look a bit narrow. */}
        <div className="content-container" style={{maxWidth: '768px'}}>
          <h2 className="form-title">Edit Your Profile</h2>
          {message && (
            <div className={message.includes('success') ? 'success-message' : 'error-message'}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit}> {/* Removed inline padding and margin */}
            <div className="label-flex-container">
              <label htmlFor="name">Full Name:</label>
              <input type="text" id="name" name="name" value={profileData.name} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="email">Email:</label>
              <input type="email" id="email" name="email" value={profileData.email} readOnly className="form-input-readonly" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="phoneNumber">Phone Number:</label>
              <input type="tel" id="phoneNumber" name="phoneNumber" value={profileData.phoneNumber} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="profession">Profession:</label>
              <input type="text" id="profession" name="profession" value={profileData.profession} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="address">Address:</label>
              <input type="text" id="address" name="address" value={profileData.address} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="postalCode">Postal Code:</label>
              <input type="text" id="postalCode" name="postalCode" value={profileData.postalCode} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="yearsOfExperience">Years of Experience:</label>
              <input type="number" id="yearsOfExperience" name="yearsOfExperience" value={profileData.yearsOfExperience} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="qualifications">Qualifications:</label>
              <textarea id="qualifications" name="qualifications" value={profileData.qualifications} onChange={handleChange} className="form-textarea" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="aboutYou">About You:</label>
              <textarea id="aboutYou" name="aboutYou" value={profileData.aboutYou} onChange={handleChange} className="form-textarea" />
            </div>
            <div className="label-flex-container">
              <label htmlFor="skills">Skills (comma-separated):</label>
              <input type="text" id="skills" name="skills" value={profileData.skills.join(', ')} onChange={handleSkillsChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="profileImageUrl">Profile Image URL:</label>
              <input type="url" id="profileImageUrl" name="profileImageUrl" value={profileData.profileImageUrl} onChange={handleChange} className="form-input"/>
            </div>

            {/* Using form-title for subtitle, with adjustments for size and spacing */}
            <h3 className="form-title" style={{fontSize: '1.5rem', paddingTop: '1.5rem', paddingBottom: '0.5rem'}}>Social & Web Links</h3>
            <div className="label-flex-container">
              <label htmlFor="linkedinProfile">LinkedIn:</label>
              <input type="url" id="linkedinProfile" name="linkedinProfile" value={profileData.linkedinProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="instagramProfile">Instagram:</label>
              <input type="url" id="instagramProfile" name="instagramProfile" value={profileData.instagramProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="facebookProfile">Facebook:</label>
              <input type="url" id="facebookProfile" name="facebookProfile" value={profileData.facebookProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="youtubeProfile">YouTube:</label>
              <input type="url" id="youtubeProfile" name="youtubeProfile" value={profileData.youtubeProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="tiktokProfile">TikTok:</label>
              <input type="url" id="tiktokProfile" name="tiktokProfile" value={profileData.tiktokProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="twitterProfile">Twitter:</label>
              <input type="url" id="twitterProfile" name="twitterProfile" value={profileData.twitterProfile} onChange={handleChange} className="form-input"/>
            </div>
            <div className="label-flex-container">
              <label htmlFor="website">Website:</label>
              <input type="url" id="website" name="website" value={profileData.website} onChange={handleChange} className="form-input"/>
            </div>

            <div className="form-group" style={{marginTop: '2rem'}}> {/* Added form-group for consistent spacing */}
              <button type="submit" className="form-button">
                Update Profile
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default ProfessionalProfilePage;
