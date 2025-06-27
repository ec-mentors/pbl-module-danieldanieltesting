import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';
import { GoogleIcon, GithubIcon } from '../components/icons';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const GOOGLE_AUTH_URL = 'http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:5173/oauth2/redirect';
  const GITHUB_AUTH_URL = 'http://localhost:8080/oauth2/authorize/github?redirect_uri=http://localhost:5173/oauth2/redirect';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const success = await login({ username, password });
      if (success) {
        navigate('/');
      } else {
        setError('Invalid username or password. Please try again.');
      }
    } catch (err) {
      setError('An unexpected error occurred during login.');
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-3xl font-bold text-center mb-6">Login to Your Account</h1>
      
      {}
      <div className="flex flex-col space-y-3 mb-6">
        <a href={GOOGLE_AUTH_URL} className="flex items-center justify-center w-full bg-white border border-gray-300 text-gray-700 font-semibold py-2 px-4 rounded-md hover:bg-gray-50">
          <GoogleIcon className="w-5 h-5 mr-2" /> Login with Google
        </a>
        <a href={GITHUB_AUTH_URL} className="flex items-center justify-center w-full bg-gray-800 text-white font-semibold py-2 px-4 rounded-md hover:bg-gray-900">
          <GithubIcon className="w-5 h-5 mr-2" /> Login with GitHub
        </a>
      </div>

      <div className="relative flex items-center my-6">
        <div className="flex-grow border-t border-gray-300"></div>
        <span className="flex-shrink mx-4 text-gray-500">OR</span>
        <div className="flex-grow border-t border-gray-300"></div>
      </div>

      {}
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="username">Username or Email</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full px-3 py-2 border rounded-md" required
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-3 py-2 border rounded-md" required
          />
        </div>
        
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        
        <button type="submit" className="w-full bg-green-600 text-white font-bold py-2 px-4 rounded-md hover:bg-green-700">
          Login
        </button>
      </form>
      <p className="text-center text-sm text-gray-600 mt-4">
        Don't have an account? <Link to="/register" className="font-medium text-blue-600 hover:underline">Register here</Link>
      </p>
    </div>
  );
};

export default LoginPage;