// src/pages/CollectionDetailPage.jsx
import React, { useState, useEffect, useCallback, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import * as api from '../services/api'; // <--- THIS IS THE FIX (was apiService)
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { FaTrash } from 'react-icons/fa';

const CollectionDetailPage = () => {
  const { collectionId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [collection, setCollection] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchCollection = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.getCollectionById(collectionId);
      setCollection(response.data);
    } catch (error) {
      toast.error("Could not load collection details.");
      navigate('/collections');
    } finally {
      setLoading(false);
    }
  }, [collectionId, navigate]);

  useEffect(() => {
    fetchCollection();
  }, [fetchCollection]);

  const handleRemovePrompt = async (promptId, promptTitle) => {
      if (!window.confirm(`Are you sure you want to remove "${promptTitle}" from this collection?`)) return;
      try {
          await api.removePromptFromCollection(collectionId, promptId);
          toast.success(`"${promptTitle}" removed from collection.`);
          // Refresh the collection data
          fetchCollection();
      } catch (error) {
          toast.error("Failed to remove prompt.");
      }
  };

  const handleDeleteCollection = async () => {
      if (!window.confirm(`Are you sure you want to DELETE the entire "${collection.name}" collection? This cannot be undone.`)) return;
      try {
          await api.deleteCollection(collectionId);
          toast.success(`Collection "${collection.name}" has been deleted.`);
          navigate('/collections');
      } catch (error) {
          toast.error("Failed to delete collection.");
      }
  }

  if (loading) return <Spinner />;
  if (!collection) return <div className="text-center text-red-500">Collection not found.</div>;

  return (
    <div className="max-w-5xl mx-auto">
      <div className="bg-white p-6 rounded-lg shadow-md mb-8">
        <div className="flex justify-between items-start">
            <div>
                <h1 className="text-4xl font-bold">{collection.name}</h1>
                <p className="text-gray-600 mt-2">{collection.description}</p>
            </div>
            <button
                onClick={handleDeleteCollection}
                className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700"
                title="Delete this entire collection"
            >
                <FaTrash /> Delete Collection
            </button>
        </div>
      </div>

      <h2 className="text-2xl font-semibold mb-4">Prompts in this Collection</h2>
      <div className="space-y-4">
        {collection.prompts && collection.prompts.length > 0 ? (
          collection.prompts.map(prompt => (
            <div key={prompt.id} className="bg-white p-4 rounded-lg shadow-sm flex justify-between items-center">
              <div>
                <Link to={`/prompts/${prompt.id}`} className="text-lg font-bold text-blue-600 hover:underline">{prompt.title}</Link>
                <p className="text-sm text-gray-500">by {prompt.authorUsername === user.username ? 'you' : prompt.authorUsername}</p>
              </div>
              <button
                onClick={() => handleRemovePrompt(prompt.id, prompt.title)}
                className="p-2 text-gray-400 hover:text-red-500 rounded-full"
                title="Remove from collection"
              >
                <FaTrash />
              </button>
            </div>
          ))
        ) : (
          <p className="text-center text-gray-500 mt-10">This collection is empty. Find some prompts to add!</p>
        )}
      </div>
    </div>
  );
};

export default CollectionDetailPage;