import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import BookmarkButton from './BookmarkButton';

const StarRating = ({ rating }) => { /* ... unchanged ... */ };

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

  const handleTagClick = (e, tag) => {
    e.stopPropagation(); // Prevent card's link from firing
    e.preventDefault(); // Prevent default button behavior
    navigate(`/prompts?tags=${tag}`);
  };

  return (
    <div className="relative flex flex-col p-6 bg-white rounded-lg border border-gray-200 shadow-md hover:shadow-lg transition-shadow duration-300 h-full">
      {isAuthenticated && (
        <div className="absolute top-2 right-2 z-10">
          <BookmarkButton promptId={prompt.id} initialIsBookmarked={prompt.isBookmarked} />
        </div>
      )}
      
      <Link to={`/prompts/${prompt.id}`} className="flex flex-col flex-grow">
        <div className="flex-grow">
          {/* --- NEW: TAGS DISPLAY --- */}
          {prompt.tags && prompt.tags.length > 0 && (
            <div className="mb-2 flex flex-wrap">
              {prompt.tags.slice(0, 3).map(tag => (
                <TagBadge key={tag} tag={tag} onClick={(e) => handleTagClick(e, tag)} />
              ))}
            </div>
          )}

          <h5 className="mb-2 text-xl font-bold tracking-tight text-gray-900 break-words hover:text-blue-700 transition-colors">
            {prompt.title}
          </h5>
          <p className="font-normal text-gray-700 mt-2 line-clamp-3">{prompt.description}</p>
        </div>

        <div className="mt-4 pt-4 border-t border-gray-200 flex justify-between items-center">
          <p className="text-sm text-gray-500">
            By: <Link to={`/profile/${prompt.authorUsername}`} onClick={e => e.stopPropagation()} className="font-medium text-blue-600 hover:underline">
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