import React, { createContext, useState, useCallback } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';

const getInitialState = () => {
  try {
    const token = localStorage.getItem('token');
    if (token) {
      const decoded = jwtDecode(token);
      if (decoded.exp * 1000 > Date.now()) {
        return {
          token: token,
          isAuthenticated: true,
          user: { username: decoded.sub },
        };
      }
    }
  } catch (error) {
    console.error("Invalid token found during initial state setup.", error);
  }
  return { token: null, isAuthenticated: false, user: null };
};

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  const [authState, setAuthState] = useState(getInitialState());

  const setAuthData = useCallback((token) => {
    try {
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
      console.error("Failed to process token:", error);
      logout();
      return false;
    }
  }, []);

  const login = async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      return setAuthData(response.data.token);
    } catch (error) {
      console.error('Login failed:', error);
      logout();
      return false;
    }
  };
  
  const loginWithToken = (token) => {
    return setAuthData(token);
  };

  const register = async (credentials) => {
    try {
      await api.post('/auth/register', credentials);
      return true;
    } catch (error) {
      console.error('Registration failed:', error.response?.data?.message);
      return error.response?.data?.message || 'Registration failed.';
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
    ...authState,
    login,
    register,
    logout,
    loginWithToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export { AuthContext, AuthProvider };