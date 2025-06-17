import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

const LoginPage = () => {
  // State for form inputs
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  // State for handling errors
  const [error, setError] = useState(null);
  
  // Get the login function from our context
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // Clear previous errors

    try {
      // Call the login function from the context
      const success = await login({ username, password });
      
      // We designed our context's login function to return true on success
      if (success) {
        navigate('/'); // Redirect to the homepage on successful login
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
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="username">
            Username
          </label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        
        {/* Display error message if it exists */}
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        
        <button
          type="submit"
          className="w-full bg-green-600 text-white font-bold py-2 px-4 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50"
        >
          Login
        </button>
      </form>
    </div>
  );
};

export default LoginPage;