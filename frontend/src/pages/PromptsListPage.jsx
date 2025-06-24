import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getPrompts, getTags } from '../services/api';
import Spinner from '../components/Spinner.jsx';
import PromptCard from '../components/PromptCard.jsx';
import Select from 'react-select';
import { useDebounce } from '../hooks/useDebounce';

const PromptsListPage = () => {
  const [prompts, setPrompts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);

  const [searchParams, setSearchParams] = useSearchParams();
  const searchTerm = searchParams.get('search') || '';
  const tagParams = searchParams.get('tags');
  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  const [availableTags, setAvailableTags] = useState([]);

  // --- FIX: USE useMemo TO PREVENT RE-CREATING THE ARRAY ON EVERY RENDER ---
  const selectedTags = useMemo(() => {
    if (!tagParams) return [];
    return tagParams.split(',').map(tag => ({ value: tag, label: tag }));
  }, [tagParams]);

  const PAGE_SIZE = 12;

  useEffect(() => {
    const fetchAllTags = async () => {
      try {
        const response = await getTags();
        setAvailableTags(response.data.map(tag => ({ value: tag, label: tag })));
      } catch (err) {
        console.error("Failed to fetch tags", err);
      }
    };
    fetchAllTags();
  }, []);

  const fetchPrompts = useCallback(async (isNewSearch) => {
    const currentPage = isNewSearch ? 0 : page;
    if (isNewSearch) setLoading(true); else setLoadingMore(true);
    setError(null);

    try {
      const params = {
        search: debouncedSearchTerm,
        page: currentPage,
        size: PAGE_SIZE,
        tags: selectedTags.length > 0 ? selectedTags.map(t => t.value).join(',') : null
      };
      
      const response = await getPrompts(params);
      const { content, totalPages: newTotalPages } = response.data;
      
      setPrompts(prev => isNewSearch ? content : [...prev, ...content]);
      setPage(currentPage + 1);
      setTotalPages(newTotalPages);
    } catch (err) {
      setError('Failed to fetch prompts. Please try again later.');
    } finally {
      if (isNewSearch) setLoading(false); else setLoadingMore(false);
    }
  }, [debouncedSearchTerm, page, selectedTags]); // selectedTags is now stable

  useEffect(() => {
    // This effect now correctly runs only when search term or tags actually change.
    fetchPrompts(true);
  }, [debouncedSearchTerm, selectedTags]);

  const handleSearchChange = (e) => {
    const newSearchTerm = e.target.value;
    const newParams = new URLSearchParams(searchParams);
    if (newSearchTerm) {
      newParams.set('search', newSearchTerm);
    } else {
      newParams.delete('search');
    }
    setSearchParams(newParams);
  };
  
  const handleTagFilterChange = (selectedOptions) => {
    const newParams = new URLSearchParams(searchParams);
    if (selectedOptions && selectedOptions.length > 0) {
      newParams.set('tags', selectedOptions.map(t => t.value).join(','));
    } else {
      newParams.delete('tags');
    }
    setSearchParams(newParams);
  };

  const handleLoadMore = () => {
    if (!loadingMore && page < totalPages) {
      fetchPrompts(false);
    }
  };

  if (loading) return <Spinner message="Loading prompts..." />;
  if (error && prompts.length === 0) return <div className="text-center mt-10 text-red-500">{error}</div>;

  return (
    <div className="container mx-auto p-4 sm:p-6 lg:p-8">
      <div className="flex justify-between items-center mb-4 flex-wrap gap-4">
        <h1 className="text-3xl font-bold">Discover Prompts</h1>
        <Link to="/create-prompt" className="bg-blue-600 text-white font-bold py-2 px-4 rounded-md hover:bg-blue-700">
          + Create New Prompt
        </Link>
      </div>

      <div className="relative z-20 grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <input
          type="text"
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Search by title, description, or text..."
          className="w-full px-4 py-2 border border-gray-300 rounded-md"
        />
        <Select
          isMulti
          options={availableTags}
          value={selectedTags}
          onChange={handleTagFilterChange}
          placeholder="Filter by tags..."
          classNamePrefix="react-select"
          instanceId="tags-filter"
        />
      </div>

      {prompts.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {prompts.map((prompt) => <PromptCard key={prompt.id} prompt={prompt} />)}
        </div>
      ) : (
        <div className="text-center mt-10 p-6 bg-white rounded-lg shadow-md">
          <p>No prompts found. Try adjusting your search or filters.</p>
        </div>
      )}

      <div className="text-center mt-8">
        {loadingMore ? <Spinner message="Loading more..." /> : (
          page < totalPages && (
            <button onClick={handleLoadMore} className="bg-gray-200 text-gray-800 font-bold py-2 px-6 rounded-md hover:bg-gray-300">
              Load More
            </button>
          )
        )}
      </div>
    </div>
  );
};

export default PromptsListPage;