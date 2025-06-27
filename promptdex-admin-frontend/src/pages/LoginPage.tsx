import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import apiClient from '../services/apiClient';

const LoginPage = () => {
  console.log('LoginPage rendering...');
  const [usernameInput, setUsernameInput] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();


  const setAuthInStore = useAuthStore((state) => state.setAuth);
  const isAdminInStore = useAuthStore((state) => state.isAdmin);
  const tokenInStore = useAuthStore((state) => state.token);


  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      console.log('LoginPage: Attempting login...');
      const response = await apiClient.post('/auth/login', { username: usernameInput, password });
      const { token: tokenFromApi } = response.data;
      if (tokenFromApi) {
        console.log('LoginPage: Token received, calling setAuthInStore.');
        setAuthInStore(tokenFromApi);
      } else {
        console.log('LoginPage: No token received from API.');
        setError('Login failed: No token received from server.');
        setAuthInStore(null);
      }
    } catch (err: any) {
      console.error('LoginPage: Login API error:', err);
      if (err.response && err.response.status === 401) {
        setError('Login failed: Invalid username or password.');
      } else {
        setError('Login failed. An unexpected error occurred.');
      }
      setAuthInStore(null);
    }
  };

  useEffect(() => {
    console.log('LoginPage useEffect - tokenInStore:', tokenInStore, 'isAdminInStore:', isAdminInStore);
    if (tokenInStore && isAdminInStore) {
      console.log('LoginPage useEffect: Admin logged in, navigating to /');
      navigate('/');
    }
  }, [tokenInStore, isAdminInStore, navigate]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <form onSubmit={handleLogin} style={{display: 'flex', flexDirection: 'column', gap: '1rem', width: '300px', padding: '2rem', border: '1px solid #ccc', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' }}>
        <h2 style={{ textAlign: 'center', marginBottom: '1.5rem' }}>Admin Panel Login</h2>
        <input
          type="text"
          placeholder="Username"
          value={usernameInput}
          onChange={(e) => setUsernameInput(e.target.value)}
          required
          style={{ padding: '0.75rem', border: '1px solid #ddd', borderRadius: '4px' }}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={{ padding: '0.75rem', border: '1px solid #ddd', borderRadius: '4px' }}
        />
        {error && <p style={{ color: 'red', textAlign: 'center', fontSize: '0.9em' }}>{error}</p>}
        <button type="submit" style={{ padding: '0.75rem', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '1em' }}>
          Login
        </button>
      </form>
    </div>
  );
};

export default LoginPage;