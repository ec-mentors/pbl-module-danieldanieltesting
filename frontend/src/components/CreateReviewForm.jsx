import React, { useState } from 'react';
import * as api from '../services/api'; 
import { toast } from 'react-toastify';
import StarRating from './StarRating'; 

const CreateReviewForm = ({ promptId, onReviewSubmitted }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      toast.warn('Please select a rating.');
      return;
    }
    if (!comment.trim()) {
      toast.warn('Please enter a comment.');
      return;
    }

    setIsSubmitting(true);
    try {
      const reviewData = { rating, comment };
      const response = await api.createReview(promptId, reviewData);
      
      toast.success('Review submitted successfully!');

      onReviewSubmitted(response.data); 
      setRating(0);
      setComment('');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit review.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mt-8 p-6 bg-gray-50 border border-gray-200 rounded-lg">
      <h3 className="text-xl font-semibold mb-4">Leave a Review</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Your Rating</label>
          {}
          <StarRating rating={rating} onRatingChange={setRating} />
        </div>
        <div>
          <label htmlFor="comment" className="block text-sm font-medium text-gray-700">Your Comment</label>
          <textarea
            id="comment"
            rows="4"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Share your thoughts on this prompt..."
          />
        </div>
        <div className="text-right">
          <button
            type="submit"
            disabled={isSubmitting}
            className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400"
          >
            {isSubmitting ? 'Submitting...' : 'Submit Review'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateReviewForm;