// src/pages/PromptDetailPage.jsx
import React, { useState, useEffect, useContext, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import * as api from '../services/api';
import { AuthContext } from '../context/AuthContext.jsx';
import Spinner from '../components/Spinner.jsx';
import BookmarkButton from '../components/BookmarkButton.jsx';
import AddToCollectionModal from '../components/AddToCollectionModal.jsx';
import { toast } from 'react-toastify';
import { FaPlusCircle, FaUser, FaTag, FaRobot, FaCalendarAlt } from 'react-icons/fa';

import ReviewCard from '../components/ReviewCard';
import CreateReviewForm from '../components/CreateReviewForm';
import StarRating from '../components/StarRating';

const PromptDetailPage = () => {
  const [prompt, setPrompt] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isCollectionModalOpen, setIsCollectionModalOpen] = useState(false);
  const { id: promptId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useContext(AuthContext);

  const fetchPrompt = useCallback(async () => {
    try {
      setLoading(true);
      const { data } = await api.getPromptById(promptId);
      setPrompt(data);
    } catch (err) {
      toast.error('Failed to fetch prompt details.');
      navigate('/prompts');
    } finally {
      setLoading(false);
    }
  }, [promptId, navigate]);

  useEffect(() => {
    fetchPrompt();
  }, [fetchPrompt]);

  const onReviewSubmitted = (newReview) => {
    setPrompt(prev => ({
        ...prev,
        reviews: [newReview, ...prev.reviews]
    }));
  };

  const onReviewDeleted = (deletedReviewId) => {
    setPrompt(prev => ({
        ...prev,
        reviews: prev.reviews.filter(review => review.id !== deletedReviewId)
    }));
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this prompt?')) {
      try {
        await api.deletePrompt(promptId);
        toast.success('Prompt deleted successfully!');
        navigate('/prompts');
      } catch (error) {
        toast.error('Failed to delete prompt.');
      }
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  };

  if (loading) return <Spinner />;
  if (!prompt) return <div className="text-center mt-20">Prompt not found.</div>;

  const isAuthor = isAuthenticated && user && user.username === prompt.authorUsername;
  const hasUserReviewed = isAuthenticated && prompt.reviews.some(review => review.authorUsername === user.username);
  const canUserReview = isAuthenticated && !isAuthor && !hasUserReviewed;
  
  // --- FIX 1: Logic to determine if "edited" timestamp should be shown ---
  const createdAt = new Date(prompt.createdAt);
  const updatedAt = new Date(prompt.updatedAt);
  const wasEdited = updatedAt.getTime() - createdAt.getTime() > 60000;

  return (
    <>
      <AddToCollectionModal isOpen={isCollectionModalOpen} onClose={() => setIsCollectionModalOpen(false)} promptId={prompt.id} promptTitle={prompt.title} />
      
      <div className="max-w-4xl mx-auto p-4 md:p-8">
        <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8 mb-8">
          {/* Header */}
          <div className="flex flex-col md:flex-row justify-between md:items-start mb-6 gap-4">
            <div className="flex-grow">
              <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 break-words">{prompt.title}</h1>
              <div className="flex items-center text-sm text-gray-500 mt-2 flex-wrap">
                <FaUser className="mr-2" /> By{' '}
                <Link to={`/profile/${prompt.authorUsername}`} className="font-semibold text-blue-600 hover:underline ml-1">
                  {prompt.authorUsername}
                </Link>
                <span className="mx-2">•</span>
                <FaCalendarAlt className="mr-2" />
                <span>{formatDate(prompt.createdAt)}</span>
                {/* --- FIX 1 (continued): Conditionally display the edited timestamp --- */}
                {wasEdited && <span className="italic text-gray-400 ml-2">(edited {formatDate(prompt.updatedAt)})</span>}
              </div>
            </div>
            <div className="flex-shrink-0 w-full md:w-auto flex items-center space-x-3">
              {isAuthenticated && !isAuthor && <BookmarkButton promptId={prompt.id} initialIsBookmarked={prompt.isBookmarked} />}
              {isAuthenticated && <button onClick={() => setIsCollectionModalOpen(true)} className="flex items-center justify-center gap-2 px-4 py-2 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700" title="Add to Collection"><FaPlusCircle /> <span className="hidden sm:inline">Collection</span></button>}
              {isAuthor && (<>
                <Link to={`/prompts/${promptId}/edit`} className="flex-1 text-center px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700">Edit</Link>
                <button onClick={handleDelete} className="flex-1 text-center px-4 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700">Delete</button>
              </>)}
            </div>
          </div>
          
          {/* Tags */}
          {prompt.tags && prompt.tags.length > 0 && (
            <div className="mb-6 flex flex-wrap items-center gap-2">
                <FaTag className="text-gray-400" />
                {prompt.tags.map(tag => (
                    <Link key={tag} to={`/prompts?tags=${tag}`} className="bg-gray-200 text-gray-800 text-xs font-semibold px-2.5 py-1 rounded-full hover:bg-gray-300">
                    #{tag}
                    </Link>
                ))}
            </div>
          )}

          {/* Metadata Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8 p-4 bg-gray-50 rounded-lg border">
            <div className="flex items-center gap-3">
                <FaRobot size={24} className="text-blue-500"/>
                <div>
                    <h4 className="text-sm font-semibold text-gray-500">AI Model</h4>
                    <p className="font-bold text-gray-800">{prompt.model}</p>
                </div>
            </div>
            <div className="flex items-center gap-3">
                <FaTag size={24} className="text-green-500"/>
                <div>
                    <h4 className="text-sm font-semibold text-gray-500">Category</h4>
                    <p className="font-bold text-gray-800">{prompt.category}</p>
                </div>
            </div>
            <div className="flex items-center gap-3">
                {/* --- FIX 2: Use the isEditable={false} prop for display --- */}
                <StarRating rating={prompt.averageRating} isEditable={false} />
                <div className="ml-2">
                    <h4 className="text-sm font-semibold text-gray-500">Avg. Rating</h4>
                    <p className="font-bold text-gray-800">{prompt.averageRating.toFixed(1)} ({prompt.reviews.length} reviews)</p>

                </div>
            </div>
          </div>

          {/* Description and Prompt Text */}
          <div className="mb-8"><h3 className="font-bold text-gray-800 text-lg mb-2">Description</h3><p className="text-gray-700">{prompt.description}</p></div>
          <div><h2 className="text-2xl font-semibold text-gray-800 mb-4 border-b pb-2">Full Prompt Text</h2><pre className="text-gray-700 whitespace-pre-wrap font-sans bg-gray-100 p-4 rounded-md">{prompt.text}</pre></div>
        </div>

        {/* --- REVIEWS SECTION --- */}
        <div className="bg-white rounded-lg shadow-xl p-4 sm:p-6 md:p-8">
          <h2 className="text-2xl font-semibold mb-4">Community Reviews</h2>
          {canUserReview && <CreateReviewForm promptId={promptId} onReviewSubmitted={onReviewSubmitted} />}
          
          <div className="mt-6 space-y-6">
            {prompt.reviews.length > 0 ? (
                prompt.reviews.map(review => (
                    <ReviewCard key={review.id} review={review} onReviewDeleted={onReviewDeleted} />
                ))
            ) : (
                <p className="text-center text-gray-500 py-4">No reviews yet. {canUserReview ? 'Be the first to leave one!' : ''}</p>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default PromptDetailPage;