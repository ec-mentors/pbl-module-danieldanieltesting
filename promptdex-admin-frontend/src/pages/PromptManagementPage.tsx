import React, { useEffect, useState, useCallback } from 'react';
import apiClient from '../services/apiClient';

interface PromptAdminView {
  id: string;
  title: string;
  text: string;
  description: string;
  model: string;
  category: string;
  authorUsername: string;
  createdAt: string;
  updatedAt: string;
  averageRating: number;
  reviews: any[];
  tags: string[];
  isBookmarked: boolean;
}

interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: { sorted: boolean; unsorted: boolean; empty: boolean; };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { sorted: boolean; unsorted: boolean; empty: boolean; };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  return debouncedValue;
}


const PromptManagementPage: React.FC = () => {
  const [promptsPage, setPromptsPage] = useState<Page<PromptAdminView> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [promptToDelete, setPromptToDelete] = useState<PromptAdminView | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);


  const fetchPrompts = useCallback(async (searchQuery: string) => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sort: 'createdAt,desc', // Default sort
      });
      if (searchQuery) {
        params.append('searchTerm', searchQuery);
      }
      const response = await apiClient.get<Page<PromptAdminView>>(`/admin/prompts?${params.toString()}`);
      setPromptsPage(response.data);
    } catch (err: any) {
      console.error('Failed to fetch prompts:', err);
      setError(err.response?.data?.message || 'Failed to fetch prompts.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    if (debouncedSearchTerm !== undefined) {
        setCurrentPage(0); 
    }
  }, [debouncedSearchTerm]);

  useEffect(() => {
    fetchPrompts(debouncedSearchTerm);
  }, [fetchPrompts, debouncedSearchTerm, currentPage]);


  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };

  const handleDeleteClick = (prompt: PromptAdminView) => {
    setPromptToDelete(prompt);
    setShowDeleteModal(true);
    setDeleteError(null);
  };

  const confirmDelete = async () => {
    if (!promptToDelete) return;
    setIsDeleting(true);
    setDeleteError(null);
    try {
      await apiClient.delete(`/admin/prompts/${promptToDelete.id}`);
      setShowDeleteModal(false);
      setPromptToDelete(null);
      if (promptsPage && promptsPage.content.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      } else {
        fetchPrompts(debouncedSearchTerm); 
      }
    } catch (err: any) {
      console.error('Failed to delete prompt:', err);
      setDeleteError(err.response?.data?.message || 'Failed to delete prompt.');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setPromptToDelete(null);
    setDeleteError(null);
  };

  const handlePreviousPage = () => setCurrentPage(prev => Math.max(0, prev - 1));
  const handleNextPage = () => {
    if (promptsPage) {
      setCurrentPage(prev => Math.min(promptsPage.totalPages - 1, prev + 1));
    }
  };
  
  const searchInputStyle: React.CSSProperties = {
    padding: '10px',
    fontSize: '1em',
    border: '1px solid #ccc',
    borderRadius: '4px',
    marginRight: '10px',
    width: '300px',
    marginBottom: '20px',
  };

  return (
    <div>
      <h2>Prompt Management</h2>

      {}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="Search prompts (title, category, author, tags...)"
          value={searchTerm}
          onChange={handleSearchChange}
          style={searchInputStyle}
        />
      </div>
      {}

      {loading && <p>Loading prompt list...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {(!promptsPage || promptsPage.content.length === 0) && !loading && !error && <p>No prompts found for the current search/filters.</p>}


      {promptsPage && promptsPage.content.length > 0 && (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '0px', tableLayout: 'fixed' }}>
            <thead>
              <tr style={{ backgroundColor: '#f2f2f2' }}>
                <th style={{...tableHeaderStyle, width: '25%'}}>Title</th>
                <th style={{...tableHeaderStyle, width: '15%'}}>Author</th>
                <th style={{...tableHeaderStyle, width: '15%'}}>Category</th>
                <th style={{...tableHeaderStyle, width: '20%'}}>Tags</th>
                <th style={{...tableHeaderStyle, width: '15%'}}>Created At</th>
                <th style={{...tableHeaderStyle, width: '10%'}}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {promptsPage.content.map((prompt) => (
                <tr key={prompt.id} style={{ borderBottom: '1px solid #ddd' }}>
                  <td style={tableCellStyle} title={prompt.title}>
                    {prompt.title.length > 40 ? `${prompt.title.substring(0, 37)}...` : prompt.title}
                  </td>
                  <td style={tableCellStyle}>{prompt.authorUsername}</td>
                  <td style={tableCellStyle}>{prompt.category}</td>
                  <td style={tableCellStyle} title={prompt.tags.join(', ')}>
                    {(prompt.tags.join(', ')).length > 30 ? `${(prompt.tags.join(', ')).substring(0,27)}...` : prompt.tags.join(', ')}
                  </td>
                  <td style={tableCellStyle}>{new Date(prompt.createdAt).toLocaleDateString()}</td>
                  <td style={tableCellStyle}>
                    <button
                      onClick={() => handleDeleteClick(prompt)}
                      style={{...actionButtonStyle, backgroundColor: '#dc3545', color: 'white'}}
                      disabled={isDeleting}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
                Total Prompts: {promptsPage.totalElements}
            </div>
            <div>
                <button onClick={handlePreviousPage} disabled={promptsPage.first || loading} style={paginationButtonStyle}>
                Previous
                </button>
                <span style={{ margin: '0 10px' }}>
                Page {promptsPage.number + 1} of {promptsPage.totalPages}
                </span>
                <button onClick={handleNextPage} disabled={promptsPage.last || loading} style={paginationButtonStyle}>
                Next
                </button>
            </div>
          </div>
        </>
      )}

      {showDeleteModal && promptToDelete && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <h3>Confirm Deletion</h3>
            <p>Are you sure you want to delete the prompt titled: "<strong>{promptToDelete.title}</strong>" by {promptToDelete.authorUsername}?</p>
            <p>This action cannot be undone.</p>
            {deleteError && <p style={{color: 'red'}}>{deleteError}</p>}
            <div style={{ marginTop: '20px', textAlign: 'right' }}>
              <button onClick={handleCloseDeleteModal} style={modalButtonStyle} disabled={isDeleting}>Cancel</button>
              <button
                onClick={confirmDelete}
                style={{...modalButtonStyle, backgroundColor: '#dc3545', color: 'white', marginLeft: '10px'}}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete Prompt'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const tableHeaderStyle: React.CSSProperties = { padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd', fontSize: '0.9em' };
const tableCellStyle: React.CSSProperties = { padding: '10px', textAlign: 'left', fontSize: '0.9em', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' };
const actionButtonStyle: React.CSSProperties = { padding: '5px 8px', marginRight: '5px', cursor: 'pointer', fontSize: '0.85em', border: 'none', borderRadius: '4px' };
const paginationButtonStyle: React.CSSProperties = { padding: '8px 12px', margin: '0 5px', cursor: 'pointer' };
const modalOverlayStyle: React.CSSProperties = {
  position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.6)',
  display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000,
};
const modalContentStyle: React.CSSProperties = {
  backgroundColor: 'white', padding: '25px', borderRadius: '8px',
  boxShadow: '0 5px 15px rgba(0, 0, 0, 0.3)', width: '450px', maxWidth: '90%',
};
const modalButtonStyle: React.CSSProperties = {
  padding: '10px 18px', borderRadius: '4px', cursor: 'pointer', fontSize: '1em', border: '1px solid #ccc'
};

export default PromptManagementPage;