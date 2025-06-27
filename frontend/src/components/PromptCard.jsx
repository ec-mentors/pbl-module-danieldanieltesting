import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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

const TagBadge = ({ tag, onClick }) => (
  <button
    onClick={onClick}
    className="bg-gray-200 text-gray-600 text-xs font-medium mr-2 px-2.5 py-0.5 rounded-full hover:bg-gray-300"
  >
    #{tag}
  </button>
);

const PromptCard = ({ prompt }) => {
  const { isAuthenticated } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleCardClick = () => {
    navigate(`/prompts/${prompt.id}`);
  };

  const handleInnerClick = (e) => {
    e.stopPropagation();
  };
  
  const handleTagClick = (e, tag) => {
    e.stopPropagation();
    navigate(`/prompts?tags=${tag}`);
  };

  return (
    <div 
      className="relative flex flex-col p-6 bg-white rounded-lg border border-gray-200 shadow-md hover:shadow-lg transition-shadow duration-300 h-full cursor-pointer"
      onClick={handleCardClick}
    >
      {isAuthenticated && (
        <div className="absolute top-2 right-2 z-10" onClick={handleInnerClick}>
          <BookmarkButton
            promptId={prompt.id}
            initialIsBookmarked={prompt.isBookmarked}
          />
        </div>
      )}
      
      {}
      <div className="flex flex-col flex-grow">
        <div className="flex-grow">
          {prompt.tags && prompt.tags.length > 0 && (
            <div className="mb-2 flex flex-wrap" onClick={handleInnerClick}>
              {prompt.tags.slice(0, 3).map(tag => (
                <TagBadge key={tag} tag={tag} onClick={(e) => handleTagClick(e, tag)} />
              ))}
            </div>
          )}

          {}
          <h5 className="mb-2 text-xl font-bold tracking-tight text-gray-900 break-words">
            <Link to={`/prompts/${prompt.id}`} onClick={handleInnerClick} className="hover:text-blue-700 transition-colors static-link">
              {prompt.title}
            </Link>
          </h5>
          <p className="font-normal text-gray-700 mt-2 line-clamp-3">{prompt.description}</p>
        </div>

        <div className="mt-4 pt-4 border-t border-gray-200 flex justify-between items-center">
          <p className="text-sm text-gray-500">
            By: <Link to={`/profile/${prompt.authorUsername}`} onClick={handleInnerClick} className="font-medium text-blue-600 hover:underline">
              {prompt.authorUsername}
            </Link>
          </p>
          <StarRating rating={prompt.averageRating} />
        </div>
      </div>
    </div>
  );
};

export default PromptCard;