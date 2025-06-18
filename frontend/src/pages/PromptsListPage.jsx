// frontend/src/pages/PromptsListPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Spinner from '../components/Spinner.jsx';

// --- NEW, MORE ROBUST HELPER COMPONENT START ---
const StarRating = ({ rating }) => {
  // Ensure rating is a valid number, default to 0 if null/undefined.
  const numericRating = Number(rating) || 0;

  // Round to the nearest whole number for display on the card.
  const fullStars = Math.round(numericRating);
  const emptyStars = 5 - fullStars;

  // This component will now ALWAYS return 5 stars (a mix of filled and empty).
  // It no longer returns the "Not yet rated" text, providing a consistent look.
  return (
    <div className="flex items-center">
      {/* Render filled stars */}
      {Array.from({ length: fullStars }, (_, i) => (
        <span key={`star-full-${i}`} className="text-yellow-500">★</span>
      ))}
      {/* Render empty stars */}
      {Array.from({ length: emptyStars }, (_, i) => (
        <span key={`star-empty-${i}`} className="text-gray-300">☆</span>
      ))}
    </div>
  );
};
// --- NEW, MORE ROBUST HELPER COMPONENT END ---


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
    return <Spinner message="Loading prompts..." />;
  }

  if (error) {
    return <div className="text-center mt-10 text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto p-4 sm:p-6 lg:p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Discover Prompts</h1>
        <Link 
          to="/create-prompt" 
          className="bg-blue-600 text-white font-bold py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          + Create New Prompt
        </Link>
      </div>

      {prompts.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {prompts.map((prompt) => (
            <Link 
              to={`/prompts/${prompt.id}`} 
              key={prompt.id} 
              className="flex flex-col p-6 bg-white rounded-lg border border-gray-200 shadow-md hover:bg-gray-100 transition duration-300"
            >
              <div className="flex-grow">
                <h5 className="mb-2 text-2xl font-bold tracking-tight text-gray-900 break-words">{prompt.title}</h5>
                <p className="font-normal text-gray-700">{prompt.description}</p>
              </div>

              <div className="mt-4 pt-4 border-t border-gray-200 flex justify-between items-center">
                <p className="text-sm text-gray-500">By: {prompt.authorUsername}</p>
                <StarRating rating={prompt.averageRating} />
              </div>
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