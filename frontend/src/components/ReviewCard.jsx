import React from 'react';
import StarRating from './StarRating';
import { formatDistanceToNow } from 'date-fns';

const ReviewCard = ({ review }) => {
  const avatarPlaceholder = `https://ui-avatars.com/api/?name=${encodeURIComponent(review.authorUsername)}&background=0D8ABC&color=fff&size=48`;

  const timeAgo = review.createdAt 
    ? formatDistanceToNow(new Date(review.createdAt), { addSuffix: true })
    : 'just now';

  return (
    <div className="flex items-start space-x-4">
      <img
        className="w-12 h-12 rounded-full object-cover flex-shrink-0"
        src={avatarPlaceholder}
        alt={review.authorUsername}
      />

      <div className="flex-1 min-w-0">
        {/* This single flex container correctly aligns the name, stars, and timestamp. */}
        <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
          <p className="font-bold text-gray-800 text-lg truncate">{review.authorUsername}</p>
          <StarRating rating={review.rating} />
          <p className="text-sm text-gray-500">{timeAgo}</p>
        </div>

        <p className="mt-2 text-gray-700 leading-relaxed break-words">{review.comment}</p>
      </div>
    </div>
  );
};

export default ReviewCard;