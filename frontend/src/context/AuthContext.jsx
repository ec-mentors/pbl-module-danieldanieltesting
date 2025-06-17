import React, { createContext, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';

// --- Helper function to get initial state ---
const getInitialState = () => {
  try {
    const token = localStorage.getItem('token');
    if (token) {
      const decoded = jwtDecode(token);
      // Check if the token is expired
      if (decoded.exp * 1000 > Date.now()) {
        // If token is valid, set the api header and return the user state
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        return {
          token: token,
          isAuthenticated: true,
          user: { username: decoded.sub },
        };
      }
    }
  } catch (error) {
    // If token is invalid or decoding fails, fall through to the default state
    console.error("Invalid token found during initial state setup.", error);
  }
  
  // Default state if no valid token is found
  localStorage.removeItem('token'); // Clean up any bad token
  return {
    token: null,
    isAuthenticated: false,
    user: null,
  };
};

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  // Initialize state from our helper function. This runs only ONCE.
  const [authState, setAuthState] = useState(getInitialState());

  const login = async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      const { token } = response.data;
      localStorage.setItem('token', token);
      
      const decoded = jwtDecode(token);
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setAuthState({
        token: token,
        isAuthenticated: true,
        user: { username: decoded.sub },
      });
      
      return true;
    } catch (error) {
      console.error('Login failed:', error);
      logout(); // Ensure clean state on failure
      return false;
    }
  };

  const register = async (credentials) => {
    try {
      await api.post('/auth/register', credentials);
      return true;
    } catch (error) {
      console.error('Registration failed:', error.response.data.message);
      return error.response.data.message || 'Registration failed.';
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    delete api.defaults.headers.common['Authorization'];
    setAuthState({
      token: null,
      isAuthenticated: false,
      user: null,
    });
  };

  const value = {
    ...authState, // Spread the state values (user, isAuthenticated, token)
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export { AuthContext, AuthProvider };