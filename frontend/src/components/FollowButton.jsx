import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import * as api from '../services/api';
import { toast } from 'react-toastify';
import { FaUserPlus, FaUserCheck, FaUserMinus } from 'react-icons/fa';

/**
 * A button for following/unfollowing. Its state is controlled by its parent.
 *
 * @param {object} props
 * @param {string} props.targetUsername The user to follow/unfollow.
 * @param {boolean} props.isFollowed The current follow status, passed from the parent.
 * @param {function} props.onUpdate A callback that receives the updated profile after a successful action.
 */
const FollowButton = ({ targetUsername, isFollowed, onUpdate }) => {
  const { isAuthenticated } = useContext(AuthContext);
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [hover, setHover] = useState(false);

  const handleToggleFollow = async (e) => {
    e.stopPropagation();
    if (!isAuthenticated) {
      toast.info('Please log in to follow users.');
      navigate('/login');
      return;
    }

    setIsLoading(true);
    try {
      const response = isFollowed
        ? await api.unfollowUser(targetUsername)
        : await api.followUser(targetUsername);

      // Notify the parent component with the fresh data from the API.
      // The parent will then update its state and pass the new `isFollowed` prop back down.
      if (onUpdate) {
        onUpdate(response.data);
      }
      
      // Toast messages are fine to keep here.
      if (response.data.isFollowedByCurrentUser) {
        toast.success(`You are now following ${targetUsername}!`);
      } else {
        toast.info(`You unfollowed ${targetUsername}.`);
      }
      
    } catch (error) {
      toast.error(error.response?.data?.message || 'Something went wrong.');
    } finally {
      setIsLoading(false);
    }
  };

  // The button's appearance is now derived directly from the `isFollowed` prop.
  let buttonText = 'Follow';
  let buttonIcon = <FaUserPlus />;
  let buttonClasses = 'bg-blue-600 hover:bg-blue-700 text-white';

  if (isFollowed) {
    if (hover) {
      buttonText = 'Unfollow';
      buttonIcon = <FaUserMinus />;
      buttonClasses = 'bg-red-600 hover:bg-red-700 text-white';
    } else {
      buttonText = 'Following';
      buttonIcon = <FaUserCheck />;
      buttonClasses = 'bg-gray-200 text-gray-800 hover:bg-red-600 hover:text-white';
    }
  }

  return (
    <button
      onClick={handleToggleFollow}
      disabled={isLoading}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      className={`flex items-center justify-center gap-2 px-4 py-2 font-semibold rounded-lg shadow-md transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed ${buttonClasses}`}
    >
      {buttonIcon}
      {isLoading ? 'Loading...' : buttonText}
    </button>
  );
};

export default FollowButton;