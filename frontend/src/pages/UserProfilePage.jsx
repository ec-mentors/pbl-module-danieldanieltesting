import React, { useState, useEffect, useCallback, useContext } from 'react';
import { useParams } from 'react-router-dom';
import * as api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import PromptCard from '../components/PromptCard';
import FollowButton from '../components/FollowButton';

const UserProfilePage = () => {
    const { username } = useParams();
    const { user: currentUser, isAuthenticated } = useContext(AuthContext);

    const [profile, setProfile] = useState(null);
    const [prompts, setPrompts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState(null);

    const PAGE_SIZE = 9;

    useEffect(() => {
        const fetchInitialData = async () => {
            setLoading(true);
            setError(null);
            setPrompts([]);
            setPage(0);

            try {
                const [profileResponse, promptsResponse] = await Promise.all([
                    api.getUserProfile(username),
                    api.getPromptsByUsername(username, { page: 0, size: PAGE_SIZE })
                ]);

                setProfile(profileResponse.data);
                setPrompts(promptsResponse.data.content);
                setTotalPages(promptsResponse.data.totalPages);

            } catch (err) {
                if (err.response && err.response.status === 404) {
                    setError(`User "${username}" not found.`);
                } else {
                    setError('Failed to fetch user profile. Please try again.');
                }
                console.error('Error fetching initial profile data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, [username]);

    const fetchMorePrompts = useCallback(async () => {
        if (loadingMore || page >= totalPages - 1) return;

        setLoadingMore(true);
        try {
            // --- THE FIX IS HERE ---
            // We pass the parameters object directly, without wrapping it in another `params` object.
            const response = await api.getPromptsByUsername(username, { 
                page: page + 1, 
                size: PAGE_SIZE 
            });
            
            const { content } = response.data;
            setPrompts(prev => [...prev, ...content]);
            setPage(prev => prev + 1);
        } catch (err) {
            console.error('Error fetching more user prompts:', err);
            toast.error("Could not load more prompts.");
        } finally {
            setLoadingMore(false);
        }
    }, [username, page, totalPages, loadingMore]);

    const handleFollowUpdate = (updatedProfile) => {
        setProfile(updatedProfile);
    };

    if (loading) {
        return <Spinner message={`Loading profile for ${username}...`} />;
    }

    if (error) {
        return <div className="text-center mt-10 text-red-500 p-4 bg-red-100 rounded-md">{error}</div>;
    }
    
    if (!profile) return null;

    const isOwnProfile = isAuthenticated && currentUser.username === username;

    return (
        <div className="container mx-auto p-4 sm:p-6 lg:p-8">
            <div className="bg-white p-6 rounded-lg shadow-md mb-8">
                <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
                    <div className="flex-grow text-center sm:text-left">
                        <h1 className="text-3xl font-bold">{profile.username}</h1>
                        <div className="flex justify-center sm:justify-start gap-6 mt-2 text-gray-600">
                            <span><span className="font-bold">{profile.followerCount}</span> Followers</span>
                            <span><span className="font-bold">{profile.followingCount}</span> Following</span>
                        </div>
                    </div>
                    {!isOwnProfile && isAuthenticated && (
                         <FollowButton 
                            targetUsername={username} 
                            isFollowed={profile.isFollowedByCurrentUser}
                            onUpdate={handleFollowUpdate}
                         />
                    )}
                </div>
            </div>

            <h2 className="text-2xl font-bold mb-6 border-b pb-4">
                Prompts by <span className="text-blue-600">{username}</span>
            </h2>

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
                                onClick={fetchMorePrompts}
                                className="bg-gray-200 text-gray-800 font-bold py-2 px-6 rounded-md hover:bg-gray-300"
                            >
                                Load More
                            </button>
                        )}
                    </div>
                </>
            ) : (
                <div className="text-center mt-10 p-6 bg-white rounded-lg shadow-md">
                    <p className="text-gray-600">{username} has not posted any prompts yet.</p>
                </div>
            )}
        </div>
    );
};

export default UserProfilePage;