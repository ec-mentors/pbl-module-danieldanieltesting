// src/components/ProtectedRoute.tsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const ProtectedRoute = () => {
  // Get the isAdmin status and token from the store.
  // The authStore is now the source of truth for whether the current session is for an admin.
  const isAdmin = useAuthStore((state) => state.isAdmin);
  const token = useAuthStore((state) => state.token); // Also check token for completeness

  if (!token || !isAdmin) {
    // If there's no token in the store, OR if the stored user is not an admin,
    // redirect to the login page. This is the primary security gate for admin routes.
    return <Navigate to="/login" replace />;
  }

  // If a token exists AND the user is recognized as an admin by the store,
  // render the child route (the actual admin page).
  return <Outlet />;
};

export default ProtectedRoute;