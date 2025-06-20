// src/components/ReviewCard.jsx
import React, { useContext } from 'react';
import StarRating from './StarRating';
import { formatDistanceToNow } from 'date-fns';
import { AuthContext } from '../context/AuthContext'; // Import AuthContext
import * as api from '../services/api'; // Import api service
import { toast } from 'react-toastify';
import { FaTrash } from 'react-icons/fa';

// Accept onReviewDeleted as a prop
const ReviewCard = ({ review, onReviewDeleted }) => { 
  const { user } = useContext(AuthContext);

  const avatarPlaceholder = `https://ui-avatars.com/api/?name=${encodeURIComponent(review.authorUsername)}&background=0D8ABC&color=fff&size=48`;

  const timeAgo = review.createdAt 
    ? formatDistanceToNow(new Date(review.createdAt), { addSuffix: true })
    : 'just now';

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete your review?')) {
      try {
        await api.deleteReview(review.id);
        toast.success('Review deleted.');
        onReviewDeleted(review.id); // Notify parent component
      } catch (error) {
        toast.error('Failed to delete review.');
      }
    }
  };

  return (
    <div className="flex items-start space-x-4">
      <img
        className="w-12 h-12 rounded-full object-cover flex-shrink-0"
        src={avatarPlaceholder}
        alt={review.authorUsername}
      />
      <div className="flex-1 min-w-0">
        <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <p className="font-bold text-gray-800 text-lg truncate">{review.authorUsername}</p>
            {/* Pass isEditable={false} to your StarRating if it supports it */}
            <StarRating rating={review.rating} /> 
            <p className="text-sm text-gray-500">{timeAgo}</p>
          </div>
          {/* Add delete button if the logged-in user is the author of the review */}
          {user?.username === review.authorUsername && (
            <button
              onClick={handleDelete}
              className="text-gray-400 hover:text-red-500"
              title="Delete my review"
            >
              <FaTrash />
            </button>
          )}
        </div>
        <p className="mt-2 text-gray-700 leading-relaxed break-words">{review.comment}</p>
      </div>
    </div>
  );
};

export default ReviewCard;