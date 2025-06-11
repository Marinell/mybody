import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Added useNavigate
// import './UserTypeSelectionPage.css'; // If you create a CSS file

const UserTypeSelectionPage = () => {
  const navigate = useNavigate(); // For button clicks if Link is not suitable for styling

  // Tailwind classes used in the original HTML are substantial.
  // For this conversion, I'll simplify the structure and styling.
  // Applying exact Tailwind classes as inline styles or via CSS modules
  // would be a larger effort. This focuses on basic structure and functionality.

  // Replicating SVG directly in JSX can be verbose.
  // It's often better to save SVG as a .svg file and import it as a component or use an <img> tag.
  // For now, I'll omit the SVG for brevity in this step.
  const FitConnectLogo = () => (
    <div className="flex items-center gap-4 text-white">
      <div className="size-4">{/* SVG placeholder */}</div>
      <h2 className="text-white text-lg font-bold leading-tight tracking-[-0.015em]">FitConnect</h2>
    </div>
  );

  return (
    <div className="relative flex size-full min-h-screen flex-col bg-[#101323] text-white font-[Manrope]">
      <div className="layout-container flex h-full grow flex-col">
        <header className="flex items-center justify-between whitespace-nowrap border-b border-solid border-b-[#21284a] px-10 py-3">
          <FitConnectLogo />
        </header>
        <div className="px-4 sm:px-10 md:px-40 flex flex-1 justify-center py-5">
          <div className="layout-content-container flex flex-col w-full max-w-lg py-5 flex-1">
            <h2 className="tracking-light text-[28px] font-bold leading-tight px-4 text-center pb-3 pt-5">Join FitConnect</h2>
            <p className="text-base font-normal leading-normal pb-3 pt-1 px-4 text-center">
              Connect with top-tier professionals in sports, fitness, and wellness to achieve your goals.
            </p>
            <div className="flex justify-center">
              <div className="flex flex-1 gap-3 max-w-md flex-col items-stretch px-4 py-3">
                {/* Using useNavigate for buttons that were not originally anchor tags */}
                <button
                  onClick={() => navigate('/login?userType=professional')}
                  className="flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-5 bg-[#607afb] text-white text-base font-bold leading-normal tracking-[0.015em] w-full"
                >
                  <span className="truncate">I'm a Professional</span>
                </button>
                <button
                  onClick={() => navigate('/login?userType=client')}
                  className="flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-5 bg-[#21284a] text-white text-base font-bold leading-normal tracking-[0.015em] w-full"
                >
                  <span className="truncate">I'm a Client</span>
                </button>
              </div>
            </div>
            <p className="text-center mt-4">
              Don't have an account?{' '}
              <Link to="/signup-client" className="text-[#607afb] hover:underline">Sign Up as Client</Link> | {' '}
              <Link to="/signup-professional" className="text-[#607afb] hover:underline">Sign Up as Professional</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
export default UserTypeSelectionPage;
