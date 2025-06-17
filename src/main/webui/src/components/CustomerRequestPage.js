import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient, getUserInfo, logout } from '../services/app';

const CustomerRequestPage = () => {
  const [userInfo, setUserInfo] = useState(null);
  // Professionals are not directly selected on this page in the original HTML.
  // This page seems to be for submitting a general request.
  // The example structure had a professionals dropdown, which might be a desired enhancement.
  // For now, I'll stick closer to the original HTML's apparent flow.
  // const [professionals, setProfessionals] = useState([]);

  const [formData, setFormData] = useState({
    category: 'one', // Default to "Select a category"
    service: 'one',  // Default to "Select a service"
    description: '',
    budget: 'one',   // Default to "Select your budget"
  });
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const currentUser = getUserInfo();
    if (!currentUser || currentUser.role !== 'CLIENT') {
      logout(); // Or navigate to login
      return;
    }
    setUserInfo(currentUser);

    // If professionals were to be listed for selection:
    // const fetchProfessionals = async () => {
    //   try {
    //     const data = await apiClient('/professionals');
    //     setProfessionals(data);
    //   } catch (error) {
    //     setMessage('Failed to fetch professionals.');
    //   }
    // };
    // fetchProfessionals();
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    if (formData.category === "one" || !formData.description.trim()) {
        setMessage('Category and description are required.');
        return;
    }

    let finalDescription = formData.description;
    if (formData.service && formData.service !== "one") {
        finalDescription = `Service: ${formData.service}. Needs: ${formData.description}`;
    }

    const payload = {
      category: formData.category,
      serviceDescription: finalDescription, // Original JS combines service and description
      budget: (formData.budget === "one" ? "" : formData.budget)
      // preferredDateTime and professionalId are not in the original customer_request.html form
    };

    try {
      const data = await apiClient('/service-requests', 'POST', payload);
      setMessage('Request submitted successfully! Finding matches...');
      // Redirect to top_matches page with the new serviceRequestId
      setTimeout(() => {
          navigate(`/top-matches?serviceRequestId=${data.id}`);
      }, 1500);
    } catch (error) {
      setMessage(error.data?.message || error.message || 'Failed to submit request.');
    }
  };

  if (!userInfo) return <p style={{color: 'white', textAlign: 'center', paddingTop: '2rem'}}>Loading...</p>;

  // Simplified JSX, Tailwind classes omitted for brevity.
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}> {/* Global body styles for theme, flex for layout */}
      <header className="app-header">
        <Link to="/" className="app-header-title"><h2>FitConnect</h2></Link>
        <div> {/* Container for right-side header items */}
          {/* Use a class that mimics app-nav button/link for logout */}
          <button onClick={logout} className="logout-nav-button">Logout</button>
          {/* Notification Bell and User Avatar Placeholder can be added here with appropriate classes */}
        </div>
      </header>
      <main className="main-container">
        <div className="content-container">
          {/* Removed extra padding div, form-title class will handle title styling */}
          <h2 className="form-title">What do you need help with?</h2>
          {message && (
            <div className={message.includes('success') ? 'success-message' : 'error-message'}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit}> {/* Removed inline padding */}
            <div className="form-group">
              <label htmlFor="categorySelect" className="form-label">Category</label>
              <select name="category" id="categorySelect" value={formData.category} onChange={handleChange} required className="form-select">
                <option value="one">Select a category</option>
                <option value="FITNESS_COACHING">Fitness Coaching</option>
                <option value="NUTRITION_CONSULTATION">Nutrition Consultation</option>
                <option value="YOGA_INSTRUCTION">Yoga Instruction</option>
                <option value="SPORTS_THERAPY">Sports Therapy</option>
                <option value="WELLNESS_PROGRAM">Wellness Program</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="serviceSelect" className="form-label">Service (Optional)</label>
              <select name="service" id="serviceSelect" value={formData.service} onChange={handleChange} className="form-select">
                <option value="one">Select a service (optional)</option>
                 <option value="PERSONAL_TRAINING">Personal Training</option>
                 <option value="DIET_PLANNING">Diet Planning</option>
                 <option value="GROUP_YOGA">Group Yoga</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="descriptionTextarea" className="form-label">Describe your needs</label>
              <textarea name="description" id="descriptionTextarea" value={formData.description} onChange={handleChange} required placeholder="Describe your needs" className="form-textarea"></textarea>
            </div>
            <div className="form-group">
              <label htmlFor="budgetSelect" className="form-label">Budget (Optional)</label>
              <select name="budget" id="budgetSelect" value={formData.budget} onChange={handleChange} className="form-select">
                <option value="one">Select your budget (optional)</option>
                <option value="UNDER_50">$ Under 50</option>
                <option value="50_100">$50 - $100</option>
                <option value="100_200">$100 - $200</option>
                <option value="ABOVE_200">$ Above 200</option>
              </select>
            </div>
            {/* form-group can also be used for button container for consistent spacing if desired */}
            <div className="form-group" style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button type="submit" className="form-button" style={{width: 'auto'}}> {/* form-button for base, inline for auto width */}
                Submit Request
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default CustomerRequestPage;
