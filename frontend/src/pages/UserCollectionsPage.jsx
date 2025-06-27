import React, { useState, useEffect, useCallback } from 'react';
import * as api from '../services/api';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { FaPlus } from 'react-icons/fa';

const UserCollectionsPage = () => {
  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDesc, setNewCollectionDesc] = useState('');

  const fetchCollections = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.getCollections();
      setCollections(response.data);
    } catch (error) {
      toast.error("Failed to load your collections.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCollections();
  }, [fetchCollections]);

  const handleCreateCollection = async (e) => {
    e.preventDefault();
    if (!newCollectionName.trim()) {
      toast.warn("Collection name is required.");
      return;
    }
    try {
      const response = await api.createCollection({ name: newCollectionName, description: newCollectionDesc });
      setCollections(prev => [...prev, response.data]);
      toast.success("Collection created successfully!");
      setNewCollectionName('');
      setNewCollectionDesc('');
      setIsCreating(false);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to create collection.");
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">My Collections</h1>
        <button
          onClick={() => setIsCreating(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700"
        >
          <FaPlus /> Create New
        </button>
      </div>
      
      {isCreating && (
        <div className="bg-white p-4 rounded-lg shadow-md mb-6">
          <form onSubmit={handleCreateCollection} className="space-y-4">
            <h3 className="text-lg font-semibold">New Collection</h3>
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">Name</label>
              <input
                type="text"
                id="name"
                value={newCollectionName}
                onChange={(e) => setNewCollectionName(e.target.value)}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
                required
              />
            </div>
            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description (Optional)</label>
              <textarea
                id="description"
                value={newCollectionDesc}
                onChange={(e) => setNewCollectionDesc(e.target.value)}
                rows="2"
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
              />
            </div>
            <div className="flex justify-end gap-3">
              <button type="button" onClick={() => setIsCreating(false)} className="px-4 py-2 bg-gray-200 rounded-md">Cancel</button>
              <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded-md">Save</button>
            </div>
          </form>
        </div>
      )}

      {collections.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {collections.map(collection => (
            <Link to={`/collections/${collection.id}`} key={collection.id} className="block bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300">
              <h2 className="text-xl font-bold text-gray-800 truncate">{collection.name}</h2>
              <p className="text-gray-600 mt-2 truncate">{collection.description || 'No description'}</p>
              <div className="mt-4 pt-4 border-t border-gray-200">
                <span className="text-sm font-semibold text-blue-600">{collection.promptCount} {collection.promptCount === 1 ? 'Prompt' : 'Prompts'}</span>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        !isCreating && <p className="text-center text-gray-500 mt-10">You haven't created any collections yet. Click "Create New" to get started!</p>
      )}
    </div>
  );
};

export default UserCollectionsPage;