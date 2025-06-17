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

  if (loading && !prompt) {
    return <Spinner />;
  }
  if (error) {
    return <div className="text-center mt-20 text-red-500">{error}</div>;
  }
  if (!prompt) {
    return <div className="text-center mt-20">Prompt not found.</div>;
  }

  const isAuthor = isAuthenticated && user?.username === prompt.authorUsername;

  return (
    <div className="max-w-4xl mx-auto p-4 md:p-8">
      {/* Main Content Card */}
      <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8 mb-8">
        {/* Responsive Header: Stacks on mobile, row on medium+ */}
        <div className="flex flex-col md:flex-row justify-between md:items-start mb-6 gap-4">
          <div className="flex-grow">
            <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 break-words">{prompt.title}</h1>
            <p className="text-md text-gray-500 mt-2">By <span className="font-semibold text-blue-600">{prompt.authorUsername}</span></p>
          </div>
          {isAuthor && (
            <div className="flex-shrink-0 w-full md:w-auto">
              <button onClick={handleDelete} className="w-full px-4 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700">Delete</button>
            </div>
          )}
        </div>
        
        {/* Responsive Grid: Stacks to 1 column on mobile automatically */}
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
          {prompt.reviews && prompt.reviews.length > 0 ? (
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

        {isAuthenticated && (<CreateReviewForm promptId={id} onReviewSubmitted={fetchPrompt} />)}
      </div>

      <div className="mt-10 text-center">
        <Link to="/prompts" className="text-blue-600 hover:underline">← Back to all prompts</Link>
      </div>
    </div>
  );
};

export default PromptDetailPage;