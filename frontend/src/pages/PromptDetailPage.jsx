// frontend/src/pages/PromptDetailPage.jsx
import React, { useState, useEffect, useContext, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext.jsx';
import CreateReviewForm from '../components/CreateReviewForm.jsx';
import Spinner from '../components/Spinner.jsx';
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

  // --- Start of render logic ---
  if (loading && !prompt) {
    return <Spinner />;
  }
  if (error) {
    return <div className="text-center mt-20 text-red-500">{error}</div>;
  }
  if (!prompt) {
    return <div className="text-center mt-20">Prompt not found.</div>;
  }

  // --- DEFINITIVE DEBUGGING LOGIC ---
  // We will calculate all boolean flags here with defensive checks.

  // 1. Is the current user the author?
  const isAuthor = !!(isAuthenticated && user && user.username === prompt.authorUsername);

  // 2. Has the current user already reviewed this prompt?
  //    This check is now super safe. It ensures prompt.reviews is an array before calling .some()
  const hasUserReviewed = !!(isAuthenticated && user && Array.isArray(prompt.reviews) && prompt.reviews.some(review => review.authorUsername === user.username));
  
  // --- CONSOLE LOGGING FOR DEBUGGING ---
  // This will print the component's state to your browser's developer console (F12).
  console.log("--- Review Logic Debug ---");
  console.log("Is Authenticated:", isAuthenticated);
  console.log("Is Author:", isAuthor);
  console.log("Has User Reviewed:", hasUserReviewed);
  console.log("Logged in User:", user ? user.username : 'Guest');
  console.log("Prompt Author:", prompt.authorUsername);
  console.log("Prompt Reviews:", prompt.reviews);
  console.log("--------------------------");
  
  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  };

  const wasEdited = new Date(prompt.updatedAt).getTime() - new Date(prompt.createdAt).getTime() > 5000;

  const renderReviewAction = () => {
    if (!isAuthenticated) {
      return (
        <div className="mt-8 p-6 text-center bg-gray-50 border border-gray-200 rounded-lg">
          <p className="text-gray-600">
            <Link to="/login" className="text-blue-600 font-semibold hover:underline">Log in</Link> or <Link to="/register" className="text-blue-600 font-semibold hover:underline">register</Link> to leave a review.
          </p>
        </div>
      );
    }
    if (isAuthor) {
      return null;
    }
    if (hasUserReviewed) {
      return (
        <div className="mt-8 p-6 text-center bg-green-50 border border-green-200 rounded-lg">
          <p className="font-semibold text-green-700">✓ You have already reviewed this prompt. Thank you!</p>
        </div>
      );
    }
    return <CreateReviewForm promptId={id} onReviewSubmitted={fetchPrompt} />;
  };


  return (
    <div className="max-w-4xl mx-auto p-4 md:p-8">
      {/* Main Content Card */}
      <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8 mb-8">
        <div className="flex flex-col md:flex-row justify-between md:items-start mb-6 gap-4">
          <div className="flex-grow">
            <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 break-words">{prompt.title}</h1>
            <div className="text-sm text-gray-500 mt-2 space-x-2">
              <span>
                By <span className="font-semibold text-blue-600">{prompt.authorUsername}</span>
              </span>
              <span>•</span>
              <span>
                Created on {formatDate(prompt.createdAt)}
              </span>
              {wasEdited && (
                <>
                  <span>•</span>
                  <span className="italic">
                    Edited on {formatDate(prompt.updatedAt)}
                  </span>
                </>
              )}
            </div>
          </div>
          {isAuthor && (
            <div className="flex-shrink-0 w-full md:w-auto flex items-center space-x-3">
              <Link
                to={`/prompts/${id}/edit`}
                className="flex-1 text-center px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700"
              >
                Edit
              </Link>
              <button
                onClick={handleDelete}
                className="flex-1 text-center px-4 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          )}
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 p-4 bg-gray-50 rounded-lg">
          <div><h3 className="font-bold text-gray-600">Category</h3><p>{prompt.category}</p></div>
          <div><h3 className="font-bold text-gray-600">Target AI Model</h3><p>{prompt.model}</p></div>
          <div><h3 className="font-bold text-gray-600">Average Rating</h3><p>{prompt.averageRating > 0 ? `${prompt.averageRating.toFixed(1)} / 5` : 'Not yet rated'}</p></div>
        </div>
        
        <div className="mb-8"><h3 className="font-bold text-gray-800 text-lg mb-2">Description</h3><p className="text-gray-700">{prompt.description}</p></div>
        <div><h2 className="text-2xl font-semibold text-gray-800 mb-4 border-b pb-2">Full Prompt Text</h2><pre className="text-gray-700 whitespace-pre-wrap font-sans bg-gray-100 p-4 rounded-md">{prompt.text}</pre></div>
      </div>
      
      {/* Reviews Section */}
      <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Reviews</h2>
        <div className="space-y-6">
          {Array.isArray(prompt.reviews) && prompt.reviews.length > 0 ? (
            prompt.reviews.map((review) => (
              <div key={review.id} className="border-b border-gray-200 pb-4">
                <div className="flex items-center mb-1">
                  <p className="font-semibold text-gray-800 mr-4">{review.authorUsername}</p>
                  <span className="text-yellow-500">{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span>
                </div>
                <p className="text-gray-600">{review.comment}</p>
              </div>
            ))
          ) : (
            <p className="text-gray-500">No reviews yet. Be the first to leave one!</p>
          )}
        </div>
        
        {renderReviewAction()}
      </div>

      <div className="mt-10 text-center">
        <Link to="/prompts" className="text-blue-600 hover:underline">← Back to all prompts</Link>
      </div>
    </div>
  );
};

export default PromptDetailPage;