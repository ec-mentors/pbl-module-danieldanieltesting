import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPrompt, getTags, updatePromptTags } from '../services/api';
import { toast } from 'react-toastify';
import CreatableSelect from 'react-select/creatable';

const CreatePromptPage = () => {
  const [title, setTitle] = useState('');
  const [text, setText] = useState('');
  const [description, setDescription] = useState('');
  const [model, setModel] = useState('');
  const [category, setCategory] = useState('');
  const [tags, setTags] = useState([]);
  const [tagOptions, setTagOptions] = useState([]); 
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const response = await getTags();
        const options = response.data.map(tag => ({ value: tag, label: tag }));
        setTagOptions(options);
      } catch (err) {
        console.error("Failed to fetch tags", err);
      }
    };
    fetchTags();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!title.trim() || !description.trim() || !category.trim() || !model.trim() || !text.trim()) {
      setError('All fields are required. Please fill out the entire form.');
      return;
    }

    setLoading(true);
    try {
      const promptData = { title, text, description, model, category };
      const response = await createPrompt(promptData);
      const newPrompt = response.data;

      if (tags && tags.length > 0) {
        const tagNames = tags.map(tag => tag.value);
        await updatePromptTags(newPrompt.id, tagNames);
      }

      toast.success('Prompt created successfully!');
      navigate('/prompts');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to create prompt. Please try again.';
      setError(errorMessage);
      toast.error(errorMessage);
      console.error('Error creating prompt:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-10 p-4 md:p-8 bg-white rounded-lg shadow-md">
      <h1 className="text-3xl font-bold text-center mb-6">Create a New Prompt</h1>
      
      <form onSubmit={handleSubmit} noValidate>
        {}
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

        {}
        <div className="mb-4">
          <label htmlFor="tags" className="block text-gray-700 text-sm font-bold mb-2">Tags</label>
          <CreatableSelect
            isMulti
            id="tags"
            instanceId="tags-create"
            value={tags}
            onChange={setTags}
            options={tagOptions}
            placeholder="Add tags (e.g., scifi, character, python)"
            classNamePrefix="react-select"
          />
        </div>

        <div className="mb-6">
          <label htmlFor="text" className="block text-gray-700 text-sm font-bold mb-2">Full Prompt Text</label>
          <textarea id="text" rows="8" value={text} onChange={(e) => setText(e.target.value)} className="w-full px-3 py-2 border rounded-md"></textarea>
        </div>
        
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        
        <button type="submit" disabled={loading} className="w-full bg-green-600 text-white font-bold py-3 px-4 rounded-md hover:bg-green-700 disabled:bg-gray-400">
          {loading ? 'Submitting...' : 'Create Prompt'}
        </button>
      </form>
    </div>
  );
};

export default CreatePromptPage;