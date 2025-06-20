import React, { useState } from 'react';
import { toast } from 'react-toastify';
import api from '../services/api';

const BookmarkButton = ({ promptId, initialIsBookmarked }) => {
  const [isBookmarked, setIsBookmarked] = useState(initialIsBookmarked);
  const [isLoading, setIsLoading] = useState(false);

  const handleToggleBookmark = async (e) => {
    // Prevent the parent Link/div click event from firing, which would navigate away.
    e.preventDefault(); 
    e.stopPropagation();
    
    setIsLoading(true);

    try {
      if (isBookmarked) {
        // --- If already bookmarked, send DELETE request to remove it ---
        await api.delete(`/prompts/${promptId}/bookmark`);
        setIsBookmarked(false);
        toast.info('Bookmark removed');
      } else {
        // --- If not bookmarked, send POST request to add it ---
        await api.post(`/prompts/${promptId}/bookmark`);
        setIsBookmarked(true);
        toast.success('Bookmark added!');
      }
    } catch (error) {
      toast.error('An error occurred. Please try again.');
      console.error("Bookmark toggle failed:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const buttonClasses = `p-2 rounded-full transition-colors duration-200 ${
    isLoading 
      ? 'cursor-not-allowed bg-gray-200' 
      : 'hover:bg-gray-200'
  }`;

  const iconClasses = `w-6 h-6 ${
    isBookmarked
      ? 'fill-current text-blue-600' // Filled icon when bookmarked
      : 'stroke-current text-gray-500 hover:text-blue-600' // Outline icon when not
  }`;
  
  return (
    <button
      onClick={handleToggleBookmark}
      disabled={isLoading}
      className={buttonClasses}
      title={isBookmarked ? 'Remove bookmark' : 'Add bookmark'}
      aria-label={isBookmarked ? 'Remove bookmark' : 'Add bookmark'}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={iconClasses}
      >
        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
      </svg>
    </button>
  );
};

export default BookmarkButton;