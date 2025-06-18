// frontend/src/pages/EditPromptPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../services/api';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner.jsx';

const EditPromptPage = () => {
  const [title, setTitle] = useState('');
  const [text, setText] = useState('');
  const [description, setDescription] = useState('');
  const [model, setModel] = useState('');
  const [category, setCategory] = useState('');
  const [loading, setLoading] = useState(true); // Start true to show spinner while fetching
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { id } = useParams(); // Get the prompt ID from the URL

  // Step 1: Fetch the existing prompt data when the component mounts
  useEffect(() => {
    const fetchPrompt = async () => {
      try {
        const response = await api.get(`/prompts/${id}`);
        const { title, text, description, model, category } = response.data;
        // Step 2: Populate the form fields with the fetched data
        setTitle(title);
        setText(text);
        setDescription(description);
        setModel(model);
        setCategory(category);
      } catch (err) {
        toast.error("Failed to load prompt data. You may not have permission to edit this.");
        navigate(`/prompts/${id}`); // Redirect if fetching fails
      } finally {
        setLoading(false); // Stop loading indicator
      }
    };

    fetchPrompt();
  }, [id, navigate]);

  // Step 3: Handle the form submission to update the prompt
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Basic validation
    if (!title.trim() || !description.trim() || !category.trim() || !model.trim() || !text.trim()) {
      setError('All fields are required. Please fill out the entire form.');
      return;
    }

    setLoading(true);
    try {
      const promptData = { title, text, description, model, category };
      // Use the PUT method to update the existing resource
      await api.put(`/prompts/${id}`, promptData);
      toast.success('Prompt updated successfully!');
      navigate(`/prompts/${id}`); // Navigate back to the detail page on success
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to update prompt. Please try again.';
      setError(errorMessage);
      toast.error(errorMessage);
      console.error('Error updating prompt:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Spinner message="Loading editor..." />;
  }

  return (
    // This form structure is reused from CreatePromptPage for consistency
    <div className="max-w-2xl mx-auto mt-10 p-4 md:p-8 bg-white rounded-lg shadow-md">
      <h1 className="text-3xl font-bold text-center mb-6">Edit Prompt</h1>
      
      <form onSubmit={handleSubmit} noValidate>
        <div className="mb-4">
          <label htmlFor="title" className="block text-gray-700 text-sm font-bold mb-2">Title</label>
          <input id="title" type="text" value={title} onChange={(e) => setTitle(e.target.value)} className="w-full px-3 py-2 border rounded-md" />
        </div>
        <div className="mb-4">
          <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">Short Description</label>
          <input id="description" type="text" value={description} onChange={(e) => setDescription(e.target.value)} className="w-full px-3 py-2 border rounded-md" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div>
            <label htmlFor="category" className="block text-gray-700 text-sm font-bold mb-2">Category</label>
            <input id="category" type="text" value={category} onChange={(e) => setCategory(e.target.value)} className="w-full px-3 py-2 border rounded-md" placeholder="e.g., Marketing" />
          </div>
          <div>
            <label htmlFor="model" className="block text-gray-700 text-sm font-bold mb-2">Target AI Model</label>
            <input id="model" type="text" value={model} onChange={(e) => setModel(e.target.value)} className="w-full px-3 py-2 border rounded-md" placeholder="e.g., GPT-4" />
          </div>
        </div>
        <div className="mb-6">
          <label htmlFor="text" className="block text-gray-700 text-sm font-bold mb-2">Full Prompt Text</label>
          <textarea id="text" rows="8" value={text} onChange={(e) => setText(e.target.value)} className="w-full px-3 py-2 border rounded-md"></textarea>
        </div>
        
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        
        <button type="submit" disabled={loading} className="w-full bg-blue-600 text-white font-bold py-3 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-400">
          {loading ? 'Saving Changes...' : 'Save Changes'}
        </button>
      </form>
    </div>
  );
};

export default EditPromptPage;