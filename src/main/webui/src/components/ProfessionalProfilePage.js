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

  if (isLoading) return <p style={{color: '#121317', textAlign: 'center', paddingTop: '2rem'}}>Loading profile...</p>;

  // Simplified JSX structure for an editable form
  return (
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: 'white', color: '#121317', minHeight: '100vh' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #f1f1f4', padding: '0.75rem 2.5rem' }}>
        <Link to="/professional-dashboard" style={{textDecoration: 'none', color: '#121317'}}><h2>FitConnect Dashboard</h2></Link>
        <button onClick={logout} style={{color: '#121317', background: 'none', border: 'none', cursor: 'pointer'}}>Logout</button>
      </header>
      <main style={{ display: 'flex', justifyContent: 'center', padding: '1.25rem 2.5rem' }}>
        <div style={{ width: '100%', maxWidth: '768px' /* md:max-w-3xl */ }}>
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', textAlign: 'center', padding: '1rem' }}>Edit Your Profile</h2>
          {message && (
            <div style={{ textAlign: 'center', padding: '0.75rem', margin: '1rem 0', borderRadius: '0.5rem', color: 'white', backgroundColor: message.includes('success') ? '#10B981' : '#EF4444' }}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit} style={{ padding: '0.75rem 1rem', marginTop: '1rem' }}>
            {/* Example fields - add all relevant fields from profileData */}
            <div style={{ marginBottom: '1rem' }}><label>Full Name: <input type="text" name="name" value={profileData.name} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Email: <input type="email" name="email" value={profileData.email} readOnly style={{width: '100%', padding: '0.5rem', backgroundColor: '#e0e0e0' /* Readonly field style */}} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Phone Number: <input type="tel" name="phoneNumber" value={profileData.phoneNumber} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Profession: <input type="text" name="profession" value={profileData.profession} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Address: <input type="text" name="address" value={profileData.address} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Postal Code: <input type="text" name="postalCode" value={profileData.postalCode} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Years of Experience: <input type="number" name="yearsOfExperience" value={profileData.yearsOfExperience} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Qualifications: <textarea name="qualifications" value={profileData.qualifications} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', minHeight: '3rem'}} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>About You: <textarea name="aboutYou" value={profileData.aboutYou} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4', minHeight: '3rem'}} /></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Skills (comma-separated): <input type="text" name="skills" value={profileData.skills.join(', ')} onChange={handleSkillsChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Profile Image URL: <input type="url" name="profileImageUrl" value={profileData.profileImageUrl} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>

            <h3 style={{fontSize: '1.25rem', fontWeight: 'bold', marginTop: '1.5rem', marginBottom: '0.5rem'}}>Social & Web Links</h3>
            <div style={{ marginBottom: '1rem' }}><label>LinkedIn: <input type="url" name="linkedinProfile" value={profileData.linkedinProfile} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            <div style={{ marginBottom: '1rem' }}><label>Website: <input type="url" name="website" value={profileData.website} onChange={handleChange} style={{width: '100%', padding: '0.5rem', backgroundColor: '#f0f1f4'}}/></label></div>
            {/* Add other social media inputs similarly */}

            <button type="submit" style={{ width: '100%', padding: '0.75rem 1.25rem', borderRadius: '0.5rem', backgroundColor: '#3e58da', color: 'white', fontWeight: 'bold', cursor: 'pointer', marginTop: '1rem' }}>
              Update Profile
            </button>
          </form>
        </div>
      </main>
    </div>
  );
};

export default ProfessionalProfilePage;
