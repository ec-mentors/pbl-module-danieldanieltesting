import React, { useState, useEffect, useContext, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext.jsx';
import CreateReviewForm from '../components/CreateReviewForm.jsx';
import Spinner from '../components/Spinner.jsx';
import BookmarkButton from '../components/BookmarkButton.jsx';
import { toast } from 'react-toastify';

const PromptDetailPage = () => {
  const [prompt, setPrompt] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useContext(AuthContext);

  const fetchPrompt = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.get(`/prompts/${id}`);
      setPrompt(response.data);
    } catch (err) {
      setError('Failed to fetch prompt details.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPrompt();
  }, [fetchPrompt]);

  const handleDelete = async () => {
    const isConfirmed = window.confirm('Are you sure you want to delete this prompt?');
    if (isConfirmed) {
      try {
        await api.delete(`/prompts/${id}`);
        toast.success('Prompt deleted successfully!');
        navigate('/prompts');
      } catch (err) {
        toast.error('Failed to delete prompt.');
      }
    }
  };

  if (loading && !prompt) {
    return <Spinner />;
  }
  if (error) {
    return <div className="text-center mt-20 text-red-500">{error}</div>;
  }
  if (!prompt) {
    return <div className="text-center mt-20">Prompt not found.</div>;
  }

  const isAuthor = !!(isAuthenticated && user && user.username === prompt.authorUsername);
  const hasUserReviewed = !!(isAuthenticated && user && Array.isArray(prompt.reviews) && prompt.reviews.some(review => review.authorUsername === user.username));
  
  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  };

  const wasEdited = new Date(prompt.updatedAt).getTime() - new Date(prompt.createdAt).getTime() > 5000;

  const renderReviewAction = () => { /* This function remains unchanged */ };

  return (
    <div className="max-w-4xl mx-auto p-4 md:p-8">
      <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8 mb-8">
        <div className="flex flex-col md:flex-row justify-between md:items-start mb-6 gap-4">
          <div className="flex-grow">
            <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 break-words">{prompt.title}</h1>
            <div className="text-sm text-gray-500 mt-2 space-x-2">
              {/* ... user, date, edited info ... */}
            </div>
          </div>
          <div className="flex-shrink-0 w-full md:w-auto flex items-center space-x-3">
            {/* --- NEW BOOKMARK & EDIT/DELETE LOGIC --- */}
            {isAuthenticated && !isAuthor && (
              <BookmarkButton
                promptId={prompt.id}
                initialIsBookmarked={prompt.isBookmarked}
              />
            )}
            {isAuthor && (
              <>
                <Link to={`/prompts/${id}/edit`} className="flex-1 text-center px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700">
                  Edit
                </Link>
                <button onClick={handleDelete} className="flex-1 text-center px-4 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700">
                  Delete
                </button>
              </>
            )}
          </div>
        </div>
        
        {/* --- REST OF THE DETAIL PAGE REMAINS THE SAME --- */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 p-4 bg-gray-50 rounded-lg">
          {/* ... category, model, rating info ... */}
        </div>
        <div className="mb-8"><h3 className="font-bold text-gray-800 text-lg mb-2">Description</h3><p className="text-gray-700">{prompt.description}</p></div>
        <div><h2 className="text-2xl font-semibold text-gray-800 mb-4 border-b pb-2">Full Prompt Text</h2><pre className="text-gray-700 whitespace-pre-wrap font-sans bg-gray-100 p-4 rounded-md">{prompt.text}</pre></div>
      </div>
      
      {/* ... Reviews Section ... */}
      <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8">
        {/* ... reviews list and form ... */}
      </div>

      <div className="mt-10 text-center">
        <Link to="/prompts" className="text-blue-600 hover:underline">← Back to all prompts</Link>
      </div>
    </div>
  );
};

export default PromptDetailPage;