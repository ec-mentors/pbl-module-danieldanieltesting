import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import * as api from '../services/api';
import Spinner from '../components/Spinner';
import PromptCard from '../components/PromptCard';

const HomePage = () => {
  // Use the authentication context to check if the user is logged in.
  const { isAuthenticated, user } = useContext(AuthContext);

  const [feedItems, setFeedItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);

  const PAGE_SIZE = 9;

  // --- Data fetching function for the activity feed ---
  const fetchFeed = useCallback(async (isInitialLoad) => {
    const newPage = isInitialLoad ? 0 : page + 1;
    if (isInitialLoad) setLoading(true); else setLoadingMore(true);
    setError(null);

    try {
        const response = await api.getFeed({ page: newPage, size: PAGE_SIZE });
        const { content, totalPages: newTotalPages } = response.data;
        
        // The API returns feed items, we need to extract the prompt from each.
        const prompts = content.map(item => item.prompt);
        
        setFeedItems(prev => isInitialLoad ? prompts : [...prev, ...prompts]);
        setPage(newPage);
        setTotalPages(newTotalPages);
    } catch (err) {
        setError('Failed to load your feed. Please try again later.');
        console.error('Error fetching feed:', err);
    } finally {
        if (isInitialLoad) setLoading(false); else setLoadingMore(false);
    }
  }, [page]);

  // Fetch the feed only if the user is authenticated.
  useEffect(() => {
    if (isAuthenticated) {
      fetchFeed(true);
    }
  }, [isAuthenticated]); // This effect runs when the user logs in or out.

  const handleLoadMore = () => {
    if (!loadingMore) {
        fetchFeed(false);
    }
  };
  
  // --- Conditional Rendering ---
  
  // 1. If the user is not authenticated, show the public welcome page.
  if (!isAuthenticated) {
    return (
      <div className="text-center bg-white p-10 rounded-lg shadow-lg">
        <h1 className="text-4xl font-bold">Welcome to PromptDex</h1>
        <p className="mt-4 text-lg text-gray-600">The ultimate platform for discovering and sharing AI prompts.</p>
        <div className="mt-8">
            <Link to="/register" className="bg-blue-600 text-white font-bold py-3 px-6 rounded-md hover:bg-blue-700 text-lg">
                Get Started
            </Link>
        </div>
      </div>
    );
  }

  // 2. If the user is authenticated, show the feed.
  if (loading) {
    return <Spinner message="Loading your feed..." />;
  }

  if (error) {
    return <div className="text-center mt-10 text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto">
        <h1 className="text-3xl font-bold mb-6 border-b pb-4">
            Your Activity Feed
        </h1>

        {feedItems.length > 0 ? (
            <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {feedItems.map(prompt => (
                        <PromptCard key={prompt.id} prompt={prompt} />
                    ))}
                </div>
                <div className="text-center mt-8">
                    {loadingMore && <Spinner message="Loading more..." />}
                    {!loadingMore && page < totalPages - 1 && (
                        <button
                            onClick={handleLoadMore}
                            className="bg-gray-200 text-gray-800 font-bold py-2 px-6 rounded-md hover:bg-gray-300"
                        >
                            Load More
                        </button>
                    )}
                </div>
            </>
        ) : (
            <div className="text-center mt-10 p-8 bg-white rounded-lg shadow-md">
                <h2 className="text-2xl font-semibold text-gray-800">Your feed is empty!</h2>
                <p className="text-gray-600 mt-2">
                    Follow some prompt authors to see their latest creations here.
                </p>
                <div className="mt-6">
                    <Link to="/prompts" className="bg-green-600 text-white font-bold py-3 px-6 rounded-md hover:bg-green-700">
                        Discover Prompts
                    </Link>
                </div>
            </div>
        )}
    </div>
  );
};

export default HomePage;