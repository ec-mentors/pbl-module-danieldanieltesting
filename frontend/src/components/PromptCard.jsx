import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import BookmarkButton from './BookmarkButton';

const StarRating = ({ rating }) => {
  const numericRating = Number(rating) || 0;
  const fullStars = Math.round(numericRating);
  const emptyStars = 5 - fullStars;

  return (
    <div className="flex items-center">
      {Array.from({ length: fullStars }, (_, i) => (
        <span key={`star-full-${i}`} className="text-yellow-500">★</span>
      ))}
      {Array.from({ length: emptyStars }, (_, i) => (
        <span key={`star-empty-${i}`} className="text-gray-300">☆</span>
      ))}
    </div>
  );
};

const PromptCard = ({ prompt }) => {
  const { isAuthenticated } = useContext(AuthContext);

  return (
    // Added 'relative' to allow absolute positioning of the bookmark button
    <div className="relative flex flex-col p-6 bg-white rounded-lg border border-gray-200 shadow-md hover:shadow-lg transition-shadow duration-300 h-full">
      {/* --- NEW: Bookmark Button --- */}
      {/* Conditionally render the button only if the user is logged in */}
      {isAuthenticated && (
        <div className="absolute top-2 right-2 z-10">
          <BookmarkButton
            promptId={prompt.id}
            initialIsBookmarked={prompt.isBookmarked}
          />
        </div>
      )}
      
      {/* The rest of the card now lives inside one Link for better accessibility */}
      <Link to={`/prompts/${prompt.id}`} className="flex flex-col flex-grow">
        <div className="flex-grow">
          <h5 className="mb-2 text-2xl font-bold tracking-tight text-gray-900 break-words hover:text-blue-700 transition-colors">
            {prompt.title}
          </h5>
          <p className="font-normal text-gray-700 mt-2 line-clamp-3">{prompt.description}</p>
        </div>

        <div className="mt-4 pt-4 border-t border-gray-200 flex justify-between items-center">
          <p className="text-sm text-gray-500">
            By: <Link 
              to={`/profile/${prompt.authorUsername}`} 
              // Stop the click from bubbling up to the main card's navigation
              onClick={e => e.stopPropagation()} 
              className="font-medium text-blue-600 hover:underline"
            >
              {prompt.authorUsername}
            </Link>
          </p>
          <StarRating rating={prompt.averageRating} />
        </div>
      </Link>
    </div>
  );
};

export default PromptCard;