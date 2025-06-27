import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import Spinner from '../components/Spinner';
import PromptCard from '../components/PromptCard';

const BookmarkedPromptsPage = () => {
    const [prompts, setPrompts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState(null);

    const PAGE_SIZE = 9;

    const fetchBookmarks = useCallback(async (isInitialLoad) => {
        const newPage = isInitialLoad ? 0 : page + 1;
        isInitialLoad ? setLoading(true) : setLoadingMore(true);
        setError(null);

        try {
            const response = await api.get('/users/me/bookmarks', {
                params: { page: newPage, size: PAGE_SIZE }
            });
            const { content, totalPages: newTotalPages } = response.data;
            setPrompts(prev => isInitialLoad ? content : [...prev, ...content]);
            setPage(newPage);
            setTotalPages(newTotalPages);
        } catch (err) {
            setError('Failed to fetch your bookmarked prompts.');
            console.error('Error fetching bookmarks:', err);
        } finally {
            isInitialLoad ? setLoading(false) : setLoadingMore(false);
        }
    }, [page]);

    useEffect(() => {
        fetchBookmarks(true);
    }, []);

    const handleLoadMore = () => {
        if (!loadingMore) {
            fetchBookmarks(false);
        }
    };

    if (loading) {
        return <Spinner message="Loading your bookmarks..." />;
    }

    if (error) {
        return <div className="text-center mt-10 text-red-500">{error}</div>;
    }

    return (
        <div className="container mx-auto p-4 sm:p-6 lg:p-8">
            <h1 className="text-3xl font-bold mb-6 border-b pb-4">My Bookmarks</h1>
            {prompts.length > 0 ? (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {prompts.map(prompt => (
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
                <div className="text-center mt-10 p-6 bg-white rounded-lg shadow-md">
                    <p className="text-gray-600">You haven't bookmarked any prompts yet.</p>
                </div>
            )}
        </div>
    );
};

export default BookmarkedPromptsPage;