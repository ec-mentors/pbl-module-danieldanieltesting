import React, { useEffect, useState, useCallback } from 'react';
import apiClient from '../services/apiClient';

// --- Interfaces (ReviewAdminView, Page) remain the same ---
interface ReviewAdminView {
  id: string;
  rating: number;
  comment: string;
  authorUsername: string;
  promptId: string;
  promptTitle: string;
  createdAt: string;
  updatedAt: string;
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
// --- End of interfaces ---

// Debounce hook (can be moved to a shared hooks.ts file if not already done)
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

const ReviewManagementPage: React.FC = () => {
  const [reviewsPage, setReviewsPage] = useState<Page<ReviewAdminView> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // --- SEARCH STATE ---
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearchTerm = useDebounce(searchTerm, 500);
  // --- END SEARCH STATE ---

  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [reviewToDelete, setReviewToDelete] = useState<ReviewAdminView | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  const fetchReviews = useCallback(async (searchQuery: string) => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sort: 'createdAt,desc',
      });
      if (searchQuery) {
        params.append('searchTerm', searchQuery);
      }
      const response = await apiClient.get<Page<ReviewAdminView>>(`/admin/reviews?${params.toString()}`);
      setReviewsPage(response.data);
    } catch (err: any) {
      console.error('Failed to fetch reviews:', err);
      setError(err.response?.data?.message || 'Failed to fetch reviews.');
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
    fetchReviews(debouncedSearchTerm);
  }, [fetchReviews, debouncedSearchTerm, currentPage]);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };

  const handleDeleteClick = (review: ReviewAdminView) => {
    setReviewToDelete(review);
    setShowDeleteModal(true);
    setDeleteError(null);
  };

  const confirmDelete = async () => {
    if (!reviewToDelete) return;
    setIsDeleting(true);
    setDeleteError(null);
    try {
      await apiClient.delete(`/admin/reviews/${reviewToDelete.id}`);
      setShowDeleteModal(false);
      setReviewToDelete(null);
      if (reviewsPage && reviewsPage.content.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      } else {
        fetchReviews(debouncedSearchTerm); // Re-fetch with current search term
      }
    } catch (err: any) {
      console.error('Failed to delete review:', err);
      setDeleteError(err.response?.data?.message || 'Failed to delete review.');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setReviewToDelete(null);
    setDeleteError(null);
  };

  const handlePreviousPage = () => setCurrentPage(prev => Math.max(0, prev - 1));
  const handleNextPage = () => {
    if (reviewsPage) {
      setCurrentPage(prev => Math.min(reviewsPage.totalPages - 1, prev + 1));
    }
  };

  const truncateText = (text: string, maxLength: number) => {
    if (!text) return ''; // Handle null or undefined text
    if (text.length <= maxLength) return text;
    return `${text.substring(0, maxLength)}...`;
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
      <h2>Review Management</h2>

      {/* --- SEARCH INPUT --- */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="Search reviews (comment, author, prompt title...)"
          value={searchTerm}
          onChange={handleSearchChange}
          style={searchInputStyle}
        />
      </div>
      {/* --- END SEARCH INPUT --- */}

      {loading && <p>Loading review list...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {(!reviewsPage || reviewsPage.content.length === 0) && !loading && !error && <p>No reviews found for the current search/filters.</p>}

      {reviewsPage && reviewsPage.content.length > 0 && (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '0px', tableLayout: 'fixed' }}>
            <thead>
              <tr style={{ backgroundColor: '#f2f2f2' }}>
                <th style={{...tableHeaderStyle, width: '30%'}}>Comment</th>
                <th style={{...tableHeaderStyle, width: '10%'}}>Rating</th>
                <th style={{...tableHeaderStyle, width: '15%'}}>Author</th>
                <th style={{...tableHeaderStyle, width: '20%'}}>Prompt Title</th>
                <th style={{...tableHeaderStyle, width: '15%'}}>Created At</th>
                <th style={{...tableHeaderStyle, width: '10%'}}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reviewsPage.content.map((review) => (
                <tr key={review.id} style={{ borderBottom: '1px solid #ddd' }}>
                  <td style={tableCellStyle} title={review.comment}>
                    {truncateText(review.comment, 50)}
                  </td>
                  <td style={tableCellStyle}>{review.rating}/5</td>
                  <td style={tableCellStyle}>{review.authorUsername}</td>
                  <td style={tableCellStyle} title={review.promptTitle}>
                    {truncateText(review.promptTitle, 30)}
                  </td>
                  <td style={tableCellStyle}>{new Date(review.createdAt).toLocaleDateString()}</td>
                  <td style={tableCellStyle}>
                    <button
                      onClick={() => handleDeleteClick(review)}
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
                Total Reviews: {reviewsPage.totalElements}
            </div>
            <div>
                <button onClick={handlePreviousPage} disabled={reviewsPage.first || loading} style={paginationButtonStyle}>
                Previous
                </button>
                <span style={{ margin: '0 10px' }}>
                Page {reviewsPage.number + 1} of {reviewsPage.totalPages}
                </span>
                <button onClick={handleNextPage} disabled={reviewsPage.last || loading} style={paginationButtonStyle}>
                Next
                </button>
            </div>
          </div>
        </>
      )}

      {showDeleteModal && reviewToDelete && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <h3>Confirm Deletion</h3>
            <p>Are you sure you want to delete this review by <strong>{reviewToDelete.authorUsername}</strong> for the prompt "<strong>{truncateText(reviewToDelete.promptTitle, 50)}</strong>"?</p>
            <p style={{fontStyle: 'italic', color: '#555', fontSize: '0.9em'}}>Comment: "{truncateText(reviewToDelete.comment, 100)}"</p>
            <p>This action cannot be undone.</p>
            {deleteError && <p style={{color: 'red'}}>{deleteError}</p>}
            <div style={{ marginTop: '20px', textAlign: 'right' }}>
              <button onClick={handleCloseDeleteModal} style={modalButtonStyle} disabled={isDeleting}>Cancel</button>
              <button
                onClick={confirmDelete}
                style={{...modalButtonStyle, backgroundColor: '#dc3545', color: 'white', marginLeft: '10px'}}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete Review'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Styles remain the same
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
  boxShadow: '0 5px 15px rgba(0, 0, 0, 0.3)', width: '500px', maxWidth: '90%',
};
const modalButtonStyle: React.CSSProperties = {
  padding: '10px 18px', borderRadius: '4px', cursor: 'pointer', fontSize: '1em', border: '1px solid #ccc'
};

export default ReviewManagementPage;