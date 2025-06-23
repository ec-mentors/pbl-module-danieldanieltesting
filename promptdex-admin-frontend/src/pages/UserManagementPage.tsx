import React, { useEffect, useState, useCallback } from 'react';
import apiClient from '../services/apiClient';

// --- Interfaces (UserAdminView, Page) remain the same ---
interface UserAdminView {
  id: string;
  username: string;
  email: string;
  roles: string[];
  provider: string;
}

interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
// --- End of interfaces ---

const ALL_AVAILABLE_ROLES = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR'];

// Debounce hook
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

const UserManagementPage: React.FC = () => {
  const [usersPage, setUsersPage] = useState<Page<UserAdminView> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10); // Or your preferred default

  // --- SEARCH STATE ---
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearchTerm = useDebounce(searchTerm, 500); // 500ms debounce
  // --- END SEARCH STATE ---

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingUser, setEditingUser] = useState<UserAdminView | null>(null);
  const [selectedRoles, setSelectedRoles] = useState<Set<string>>(new Set());
  const [modalError, setModalError] = useState<string | null>(null);

  const fetchUsers = useCallback(async (searchQuery: string) => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sort: 'username,asc',
      });
      if (searchQuery) {
        params.append('searchTerm', searchQuery);
      }
      const response = await apiClient.get<Page<UserAdminView>>(`/admin/users?${params.toString()}`);
      setUsersPage(response.data);
    } catch (err: any) {
      console.error('Failed to fetch users:', err);
      setError(err.response?.data?.message || 'Failed to fetch users. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]); // Removed debouncedSearchTerm from here, will handle its effect separately

  useEffect(() => {
    // Reset to page 0 when search term changes, then fetch
    // This effect handles changes to the debounced search term
    if (debouncedSearchTerm !== undefined) { // Check if it's the initial undefined or actual term
        setCurrentPage(0); // Reset to first page on new search
        // fetchUsers will be called by the effect below due to currentPage change
    }
  }, [debouncedSearchTerm]);


  useEffect(() => {
    // This effect handles fetching when currentPage, pageSize, or debouncedSearchTerm (indirectly via currentPage reset) changes
    fetchUsers(debouncedSearchTerm);
  }, [fetchUsers, debouncedSearchTerm, currentPage]); // Added currentPage here to refetch when it changes (e.g., after search term reset)


  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };

  const handleOpenEditModal = (user: UserAdminView) => {
    setEditingUser(user);
    setSelectedRoles(new Set(user.roles));
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
    setSelectedRoles(new Set());
    setModalError(null);
  };

  const handleRoleCheckboxChange = (role: string) => {
    setSelectedRoles((prevRoles) => {
      const newRoles = new Set(prevRoles);
      if (newRoles.has(role)) {
        newRoles.delete(role);
      } else {
        newRoles.add(role);
      }
      return newRoles;
    });
  };

  const handleSubmitRoles = async () => {
    if (!editingUser) return;
    setModalError(null);
    if (selectedRoles.size === 0) {
        setModalError("User must have at least one role.");
        return;
    }
    try {
      await apiClient.put(`/admin/users/${editingUser.id}/roles`, {
        roles: Array.from(selectedRoles),
      });
      handleCloseModal();
      fetchUsers(debouncedSearchTerm); // Re-fetch with current search term
    } catch (err: any) {
      console.error('Failed to update roles:', err);
      setModalError(err.response?.data?.message || 'Failed to update roles.');
    }
  };

  const handlePreviousPage = () => {
    setCurrentPage(prev => Math.max(0, prev - 1));
  };

  const handleNextPage = () => {
    if (usersPage) {
        setCurrentPage(prev => Math.min(usersPage.totalPages - 1, prev + 1));
    }
  };
  
  const searchInputStyle: React.CSSProperties = {
    padding: '10px',
    fontSize: '1em',
    border: '1px solid #ccc',
    borderRadius: '4px',
    marginRight: '10px',
    width: '300px', // Adjust as needed
    marginBottom: '20px', // Add some space below search bar
  };


  return (
    <div>
      <h2>User Management</h2>

      {/* --- SEARCH INPUT --- */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="Search by username or email..."
          value={searchTerm}
          onChange={handleSearchChange}
          style={searchInputStyle}
        />
      </div>
      {/* --- END SEARCH INPUT --- */}

      {loading && <p>Loading user list...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {(!usersPage || usersPage.content.length === 0) && !loading && !error && <p>No users found for the current search/filters.</p>}

      {usersPage && usersPage.content.length > 0 && (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '0px' }}> {/* Removed marginTop from table as search bar has margin-bottom */}
            <thead>
              <tr style={{ backgroundColor: '#f2f2f2' }}>
                <th style={tableHeaderStyle}>Username</th>
                <th style={tableHeaderStyle}>Email</th>
                <th style={tableHeaderStyle}>Roles</th>
                <th style={tableHeaderStyle}>Provider</th>
                <th style={tableHeaderStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {usersPage.content.map((user) => (
                <tr key={user.id} style={{ borderBottom: '1px solid #ddd' }}>
                  <td style={tableCellStyle}>{user.username}</td>
                  <td style={tableCellStyle}>{user.email}</td>
                  <td style={tableCellStyle}>{user.roles.join(', ')}</td>
                  <td style={tableCellStyle}>{user.provider}</td>
                  <td style={tableCellStyle}>
                    <button onClick={() => handleOpenEditModal(user)} style={actionButtonStyle}>Edit Roles</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
                {/* Optional: Show total results */}
                Total Users: {usersPage.totalElements}
            </div>
            <div>
                <button onClick={handlePreviousPage} disabled={usersPage.first || loading} style={paginationButtonStyle}>
                Previous
                </button>
                <span style={{ margin: '0 10px' }}>
                Page {usersPage.number + 1} of {usersPage.totalPages}
                </span>
                <button onClick={handleNextPage} disabled={usersPage.last || loading} style={paginationButtonStyle}>
                Next
                </button>
            </div>
          </div>
        </>
      )}

      {isModalOpen && editingUser && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <h3>Edit Roles for {editingUser.username}</h3>
            {modalError && <p style={{color: 'red'}}>{modalError}</p>}
            <form onSubmit={(e) => { e.preventDefault(); handleSubmitRoles(); }}>
              <div style={{ marginBottom: '15px' }}>
                {ALL_AVAILABLE_ROLES.map((role) => (
                  <div key={role} style={{ marginBottom: '5px' }}>
                    <label>
                      <input
                        type="checkbox"
                        checked={selectedRoles.has(role)}
                        onChange={() => handleRoleCheckboxChange(role)}
                        style={{ marginRight: '8px' }}
                      />
                      {role}
                    </label>
                  </div>
                ))}
              </div>
              <div style={{ textAlign: 'right' }}>
                <button type="button" onClick={handleCloseModal} style={modalButtonStyle}>Cancel</button>
                <button type="submit" style={{...modalButtonStyle, backgroundColor: '#007bff', color: 'white', marginLeft: '10px'}}>Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// Styles remain the same
const tableHeaderStyle: React.CSSProperties = { padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd' };
const tableCellStyle: React.CSSProperties = { padding: '12px', textAlign: 'left' };
const actionButtonStyle: React.CSSProperties = { padding: '6px 10px', marginRight: '5px', cursor: 'pointer', fontSize: '0.9em' };
const paginationButtonStyle: React.CSSProperties = { padding: '8px 12px', margin: '0 5px', cursor: 'pointer' };
const modalOverlayStyle: React.CSSProperties = {
  position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex',
  justifyContent: 'center', alignItems: 'center', zIndex: 1000,
};
const modalContentStyle: React.CSSProperties = {
  backgroundColor: 'white', padding: '25px', borderRadius: '8px',
  boxShadow: '0 4px 15px rgba(0, 0, 0, 0.2)', width: '400px',
  maxWidth: '90%',
};
const modalButtonStyle: React.CSSProperties = {
  padding: '10px 15px', border: 'none', borderRadius: '4px',
  cursor: 'pointer', fontSize: '1em',
};

export default UserManagementPage;