import React, { useState } from 'react';
import api from '../services/api';

// This component receives two props from its parent (PromptDetailPage):
// 1. promptId: The ID of the prompt being reviewed.
// 2. onReviewSubmitted: A function to call after a review is successfully submitted.
const CreateReviewForm = ({ promptId, onReviewSubmitted }) => {
  // --- State for the form inputs ---
  const [rating, setRating] = useState(5); // Default rating to 5 stars
  const [comment, setComment] = useState('');

  // --- State for handling the submission process ---
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent default page reload
    setError(null); // Clear previous errors

    // --- Client-side validation ---
    if (!comment.trim()) {
      setError('A comment is required to submit a review.');
      return; // Stop the function if validation fails
    }

    setLoading(true);

    try {
      const reviewData = {
        rating: Number(rating), // Ensure rating is a number
        comment: comment,
        promptId: promptId,
      };

      // --- Call the backend API ---
      await api.post('/reviews', reviewData);

      // --- On success, reset the form and notify the parent ---
      setComment('');
      setRating(5);
      onReviewSubmitted(); // This triggers the data refresh in PromptDetailPage

    } catch (err) {
      setError('Failed to submit review. Please try again.');
      console.error('Review submission error:', err);
    } finally {
      setLoading(false); // Stop the loading indicator
    }
  };

  return (
    <div className="mt-8 p-6 bg-gray-50 border border-gray-200 rounded-lg">
      <h3 className="text-xl font-semibold mb-4">Leave a Review</h3>
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
          <div className="md:col-span-1">
            <label htmlFor="rating" className="block text-sm font-medium text-gray-700 mb-1">Rating</label>
            <select
              id="rating"
              value={rating}
              onChange={(e) => setRating(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="5">5 Stars</option>
              <option value="4">4 Stars</option>
              <option value="3">3 Stars</option>
              <option value="2">2 Stars</option>
              <option value="1">1 Star</option>
            </select>
          </div>
          <div className="md:col-span-3">
            <label htmlFor="comment" className="block text-sm font-medium text-gray-700 mb-1">Comment</label>
            <textarea
              id="comment"
              rows="3"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Share your thoughts on this prompt..."
            ></textarea>
          </div>
        </div>

        {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
        
        <div className="text-right">
          <button
            type="submit"
            disabled={loading}
            className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400"
          >
            {loading ? 'Submitting...' : 'Submit Review'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateReviewForm;