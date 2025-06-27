import React, { useState } from 'react';
import { Outlet, useNavigate, NavLink } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const NavItem: React.FC<{ to: string; children: React.ReactNode }> = ({ to, children }) => {
  const [isHovered, setIsHovered] = useState(false);

  const navLinkBaseStyle: React.CSSProperties = {
    display: 'block',
    padding: '10px 15px',
    marginBottom: '5px',
    color: '#333',
    backgroundColor: 'transparent',
    textDecoration: 'none',
    borderRadius: '4px',
    transition: 'background-color 0.2s ease-in-out, color 0.2s ease-in-out',
    boxSizing: 'border-box',
    fontWeight: 400, 
    width: '100%',
    lineHeight: '1.5', 
    textAlign: 'left', 
  };

  const activeNavLinkStyle: React.CSSProperties = {
    backgroundColor: '#007bff',
    color: 'white',

  };

  const hoverNavLinkStyle: React.CSSProperties = {
    backgroundColor: '#e9ecef',

  };

  return (
    <NavLink
      to={to}
      style={({ isActive }) => ({
        ...navLinkBaseStyle,
        ...(isActive && activeNavLinkStyle),
        ...(isHovered && !isActive && hoverNavLinkStyle),
      })}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {children}
    </NavLink>
  );
};


const AdminLayout: React.FC = () => {
  const user = useAuthStore((state) => state.user);
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  const handleLogout = () => {
    setAuth(null);
    navigate('/login');
  };

  const layoutStyle: React.CSSProperties = {
    display: 'flex',
    minHeight: '100vh',
    fontFamily: '"Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  };

  const sidebarStyle: React.CSSProperties = {
    width: '250px',
    minWidth: '250px', 
    backgroundColor: '#f0f2f5',
    padding: '20px',
    borderRight: '1px solid #e0e0e0',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
  };

  const sidebarTitleStyle: React.CSSProperties = {
    fontSize: '1.5em',
    fontWeight: 600,
    marginBottom: '20px',
    color: '#343a40',
    flexShrink: 0,
  };

  const navStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    width: '100%',
  };

  const headerStyle: React.CSSProperties = {
    padding: '20px',
    backgroundColor: '#ffffff',
    borderBottom: '1px solid #e0e0e0',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  };

  const contentStyle: React.CSSProperties = {
    flexGrow: 1,
    display: 'flex',
    flexDirection: 'column',
  };

  const mainContentAreaStyle: React.CSSProperties = {
    padding: '20px',
    flexGrow: 1,
    backgroundColor: '#f8f9fa',
  };

  return (
    <div style={layoutStyle}>
      <aside style={sidebarStyle}>
        <h2 style={sidebarTitleStyle}>Admin Menu</h2>
        <nav style={navStyle}> {}
          <NavItem to="/">Dashboard</NavItem>
          <NavItem to="/users">User Management</NavItem>
          <NavItem to="/prompts">Prompt Management</NavItem>
          <NavItem to="/reviews">Review Management</NavItem>
        </nav>
      </aside>
      <div style={contentStyle}>
        <header style={headerStyle}>
          <div>
            {user?.username ? `Welcome, ${user.username}` : 'Admin Panel'}
          </div>
          <button
            onClick={handleLogout}
            style={{
              padding: '8px 15px',
              cursor: 'pointer',
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontWeight: 500,
            }}
          >
            Logout
          </button>
        </header>
        <main style={mainContentAreaStyle}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;