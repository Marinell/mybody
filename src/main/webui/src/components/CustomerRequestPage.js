import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient, getUserInfo, logout } from '../../js/app';

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
    <div style={{ fontFamily: 'Manrope, "Noto Sans", sans-serif', backgroundColor: '#101323', color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #21284a', padding: '0.75rem 2.5rem' }}>
        <Link to="/" style={{textDecoration: 'none', color: 'white'}}><div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}><div style={{width: '1rem', height: '1rem' /* SVG Placeholder */}}></div><h2>FitConnect</h2></div></Link>
        <div style={{display: 'flex', alignItems: 'center', gap: '1rem'}}>
            <button onClick={logout} style={{color: 'white', background: 'none', border: 'none', cursor: 'pointer'}}>Logout</button>
            {/* Notification Bell and User Avatar Placeholder */}
        </div>
      </header>
      <main style={{ display: 'flex', flex: 1, justifyContent: 'center', padding: '1.25rem 2.5rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', width: '100%', maxWidth: '512px' }}>
          <div style={{padding: '1rem'}}>
            <p style={{fontSize: '32px', fontWeight: 'bold'}}>What do you need help with?</p>
          </div>
          {message && (
            <div style={{ textAlign: 'center', padding: '0.5rem', margin: '0 1rem', color: message.includes('success') ? 'lightgreen' : 'orangered' }}>
              {message}
            </div>
          )}
          <form onSubmit={handleSubmit} style={{ padding: '0.75rem 1rem' }}>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="category" style={{display:'block', paddingBottom: '0.5rem'}}>Category</label>
              <select name="category" id="categorySelect" value={formData.category} onChange={handleChange} required style={{width: '100%', padding: '0.875rem', borderRadius: '0.75rem', backgroundColor: '#181d35', border: '1px solid #2f396a', color: 'white'}}>
                <option value="one">Select a category</option>
                {/* Populate with actual categories later if dynamic */}
                <option value="FITNESS_COACHING">Fitness Coaching</option>
                <option value="NUTRITION_CONSULTATION">Nutrition Consultation</option>
                <option value="YOGA_INSTRUCTION">Yoga Instruction</option>
                <option value="SPORTS_THERAPY">Sports Therapy</option>
                <option value="WELLNESS_PROGRAM">Wellness Program</option>
              </select>
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="service" style={{display:'block', paddingBottom: '0.5rem'}}>Service (Optional)</label>
              <select name="service" id="serviceSelect" value={formData.service} onChange={handleChange} style={{width: '100%', padding: '0.875rem', borderRadius: '0.75rem', backgroundColor: '#181d35', border: '1px solid #2f396a', color: 'white'}}>
                <option value="one">Select a service (optional)</option>
                {/* Populate based on category or general list */}
                 <option value="PERSONAL_TRAINING">Personal Training</option>
                 <option value="DIET_PLANNING">Diet Planning</option>
                 <option value="GROUP_YOGA">Group Yoga</option>
              </select>
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="description" style={{display:'block', paddingBottom: '0.5rem'}}>Describe your needs</label>
              <textarea name="description" id="descriptionTextarea" value={formData.description} onChange={handleChange} required placeholder="Describe your needs" style={{width: '100%', minHeight: '6rem', padding: '0.875rem', borderRadius: '0.75rem', backgroundColor: '#181d35', border: '1px solid #2f396a', color: 'white'}}></textarea>
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="budget" style={{display:'block', paddingBottom: '0.5rem'}}>Budget (Optional)</label>
              <select name="budget" id="budgetSelect" value={formData.budget} onChange={handleChange} style={{width: '100%', padding: '0.875rem', borderRadius: '0.75rem', backgroundColor: '#181d35', border: '1px solid #2f396a', color: 'white'}}>
                <option value="one">Select your budget (optional)</option>
                <option value="UNDER_50">$ Under 50</option>
                <option value="50_100">$50 - $100</option>
                <option value="100_200">$100 - $200</option>
                <option value="ABOVE_200">$ Above 200</option>
              </select>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '0.75rem 0' }}>
              <button type="submit" style={{ padding: '0.5rem 1rem', borderRadius: '9999px', backgroundColor: '#607afb', color: 'white', fontWeight: 'bold', cursor: 'pointer' }}>
                Submit
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default CustomerRequestPage;
