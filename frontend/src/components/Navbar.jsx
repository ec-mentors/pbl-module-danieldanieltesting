import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

const NavLink = ({ to, children, onClick }) => (
  <Link to={to} onClick={onClick} className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
    {children}
  </Link>
);

const MobileNavLink = ({ to, children, onClick }) => (
  <Link to={to} onClick={onClick} className="block text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-base font-medium">
    {children}
  </Link>
);

const Navbar = () => {
  const { isAuthenticated, user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setMobileMenuOpen(false);
    navigate('/login');
  };

  return (
    <nav className="bg-gray-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Link to="/" className="text-white font-bold text-lg">PromptDex</Link>
            </div>
            <div className="hidden md:block">
              <div className="ml-10 flex items-baseline space-x-4">
                <NavLink to="/prompts">Prompts</NavLink>
              </div>
            </div>
          </div>
          <div className="hidden md:block">
            {isAuthenticated ? (
              <div className="ml-4 flex items-center md:ml-6">
                <span className="text-gray-400 text-sm mr-4">Welcome, {user.username}!</span>
                <button onClick={handleLogout} className="bg-red-600 text-white px-3 py-2 rounded-md text-sm font-medium hover:bg-red-700">Logout</button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <NavLink to="/login">Login</NavLink>
                <Link to="/register" className="bg-blue-600 text-white px-3 py-2 rounded-md text-sm font-medium hover:bg-blue-700">Register</Link>
              </div>
            )}
          </div>
          <div className="-mr-2 flex md:hidden">
            <button
              onClick={() => setMobileMenuOpen(!isMobileMenuOpen)}
              type="button"
              className="bg-gray-800 inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
              aria-controls="mobile-menu"
              aria-expanded={isMobileMenuOpen}
            >
              <span className="sr-only">Open main menu</span>
              {isMobileMenuOpen ? (
                <svg className="block h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" /></svg>
              ) : (
                <svg className="block h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" /></svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {isMobileMenuOpen && (
        <div className="md:hidden" id="mobile-menu">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            <MobileNavLink to="/prompts" onClick={() => setMobileMenuOpen(false)}>Prompts</MobileNavLink>
          </div>
          <div className="pt-4 pb-3 border-t border-gray-700">
            {isAuthenticated ? (
              <div className="px-2 space-y-2">
                 <div className="px-3 mb-2"><p className="text-base font-medium text-white">{user.username}</p></div>
                 <button onClick={handleLogout} className="w-full text-left block px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:text-white hover:bg-gray-700">Logout</button>
              </div>
            ) : (
               <div className="px-2 space-y-2">
                 <MobileNavLink to="/login" onClick={() => setMobileMenuOpen(false)}>Login</MobileNavLink>
                 <MobileNavLink to="/register" onClick={() => setMobileMenuOpen(false)}>Register</MobileNavLink>
               </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;