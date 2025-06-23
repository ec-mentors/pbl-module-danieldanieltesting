// src/pages/DashboardPage.tsx
import React, { useEffect, useState } from 'react';
import apiClient from '../services/apiClient'; // Ensure apiClient is correctly imported

// Interface matching the backend StatsDto
interface StatsData {
  totalUsers: number;
  totalPrompts: number;
  totalReviews: number;
}

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<StatsData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<StatsData>('/admin/stats');
        setStats(response.data);
      } catch (err: any) {
        console.error("Failed to fetch dashboard stats:", err);
        setError(err.response?.data?.message || 'Failed to load statistics.');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []); // Empty dependency array means this runs once on mount

  // Simple card style for displaying stats
  const statCardStyle: React.CSSProperties = {
    backgroundColor: '#fff',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    textAlign: 'center',
    minWidth: '180px',
  };

  const statValueStyle: React.CSSProperties = {
    fontSize: '2em',
    fontWeight: 'bold',
    color: '#007bff', // Primary color for value
    margin: '0 0 5px 0',
  };

  const statLabelStyle: React.CSSProperties = {
    fontSize: '1em',
    color: '#6c757d', // Muted color for label
    margin: '0',
  };

  return (
    <div>
      <h2 style={{ marginBottom: '1.5rem', borderBottom: '1px solid #eee', paddingBottom: '0.5rem' }}>
        Dashboard Overview
      </h2>

      {loading && <p>Loading statistics...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}

      {!loading && !error && stats && (
        <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', justifyContent: 'flex-start', marginTop: '20px' }}>
          <div style={statCardStyle}>
            <p style={statValueStyle}>{stats.totalUsers}</p>
            <p style={statLabelStyle}>Total Users</p>
          </div>
          <div style={statCardStyle}>
            <p style={statValueStyle}>{stats.totalPrompts}</p>
            <p style={statLabelStyle}>Total Prompts</p>
          </div>
          <div style={statCardStyle}>
            <p style={statValueStyle}>{stats.totalReviews}</p>
            <p style={statLabelStyle}>Total Reviews</p>
          </div>
          {/* Add more stat cards here as needed */}
        </div>
      )}

      {!loading && !error && !stats && (
        <p>No statistics data available.</p>
      )}

      <div style={{ marginTop: '2rem' }}>
        <p>This dashboard provides a quick overview of your application's key metrics.</p>
        <p>
          Use the sidebar navigation to access detailed management sections for Users, Prompts, and Reviews.
        </p>
      </div>
    </div>
  );
};

export default DashboardPage;