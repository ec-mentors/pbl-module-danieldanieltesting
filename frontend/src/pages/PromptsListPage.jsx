import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Spinner from '../components/Spinner.jsx'; // Import the new spinner

const PromptsListPage = () => {
  const [prompts, setPrompts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPrompts = async () => {
      try {
        const response = await api.get('/prompts');
        setPrompts(response.data);
      } catch (err) {
        setError('Failed to fetch prompts. Please try again later.');
        console.error('Error fetching prompts:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchPrompts();
  }, []);

  if (loading) {
    // Using the new, better spinner component
    return <Spinner message="Fetching prompts..." />;
  }

  if (error) {
    return <div className="text-center mt-10 text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Discover Prompts</h1>
        <Link to="/create-prompt" className="bg-blue-600 text-white font-bold py-2 px-4 rounded-md hover:bg-blue-700">
          + Create New Prompt
        </Link>
      </div>
      {prompts.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {prompts.map((prompt) => (
            <Link to={`/prompts/${prompt.id}`} key={prompt.id} className="block p-6 bg-white rounded-lg border border-gray-200 shadow-md hover:bg-gray-100 transition duration-300">
              <h5 className="mb-2 text-2xl font-bold tracking-tight text-gray-900">{prompt.title}</h5>
              <p className="font-normal text-gray-700 truncate">{prompt.text}</p>
              <p className="mt-4 text-sm text-gray-500">By: {prompt.authorUsername}</p>
            </Link>
          ))}
        </div>
      ) : (
        <div className="text-center mt-10 p-6 bg-white rounded-lg shadow-md">
          <p>No prompts found. Be the first to create one!</p>
        </div>
      )}
    </div>
  );
};

export default PromptsListPage;