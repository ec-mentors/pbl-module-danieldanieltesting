import React, { useState, useEffect, useCallback } from 'react';
import * as api from '../services/api';
import { toast } from 'react-toastify';
import Spinner from './Spinner';
import { FaPlus, FaTimes } from 'react-icons/fa';

const AddToCollectionModal = ({ isOpen, onClose, promptId, promptTitle }) => {
  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');

  const fetchCollections = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.getCollections();
      setCollections(response.data);
    } catch (error) {
      toast.error("Failed to fetch your collections.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      fetchCollections();
    }
  }, [isOpen, fetchCollections]);


  const handleAddToCollection = async (collectionId) => {
    try {

      const response = await api.addPromptToCollection(collectionId, promptId);
      const updatedCollection = response.data; 
      toast.success(`'${promptTitle}' added to '${updatedCollection.name}'!`);
      onClose(); 
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to add prompt to collection.");
    }
  };

  const handleCreateAndAdd = async (e) => {
      e.preventDefault();
      if (!newCollectionName.trim()) {
          toast.warn("Collection name cannot be empty.");
          return;
      }
      try {
          const createResponse = await api.createCollection({ name: newCollectionName, description: '' });
          const newCollection = createResponse.data;

          await handleAddToCollection(newCollection.id);
          
          setIsCreating(false);
          setNewCollectionName('');
      } catch (error) {
           toast.error(error.response?.data?.message || "Failed to create and add to collection.");
      }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div className="p-4 border-b flex justify-between items-center">
          <h2 className="text-xl font-bold">Add to Collection</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-800">
            <FaTimes size={20} />
          </button>
        </div>
        <div className="p-4">
          {loading ? <Spinner /> : (
            <div className="space-y-3 max-h-60 overflow-y-auto">
              {collections.map(collection => (
                <button
                  key={collection.id}
                  onClick={() => handleAddToCollection(collection.id)}
                  className="w-full text-left p-3 bg-gray-100 rounded-md hover:bg-gray-200"
                >
                  <p className="font-semibold">{collection.name}</p>
                  <p className="text-sm text-gray-500">{collection.promptCount} prompts</p>
                </button>
              ))}
              {collections.length === 0 && <p className="text-gray-500 text-center">You have no collections yet.</p>}
            </div>
          )}
          <div className="mt-4 pt-4 border-t">
            {isCreating ? (
              <form onSubmit={handleCreateAndAdd} className="space-y-2">
                <input
                  type="text"
                  value={newCollectionName}
                  onChange={(e) => setNewCollectionName(e.target.value)}
                  placeholder="New collection name"
                  className="w-full p-2 border rounded-md"
                  autoFocus
                />
                <div className="flex justify-end gap-2">
                   <button type="button" onClick={() => setIsCreating(false)} className="px-4 py-2 bg-gray-200 rounded-md">Cancel</button>
                   <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded-md">Create & Add</button>
                </div>
              </form>
            ) : (
              <button
                onClick={() => setIsCreating(true)}
                className="w-full flex items-center justify-center gap-2 p-3 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                <FaPlus /> Create New Collection
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddToCollectionModal;