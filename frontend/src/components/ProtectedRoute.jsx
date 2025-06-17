import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

const ProtectedRoute = () => {
  // 1. Get the authentication state from our global context.
  const { isAuthenticated } = useContext(AuthContext);

  // You can also add a check for a "loading" state if your context has one,
  // to show a spinner while it checks for a token on initial load.
  // For now, we'll keep it simple.

  // 2. Check if the user is authenticated.
  if (!isAuthenticated) {
    // 3. If not authenticated, redirect them to the login page.
    // The `replace` prop is important. It replaces the current entry in the
    // history stack instead of adding a new one. This prevents the user from
    // clicking the "back" button and ending up in a weird state.
    return <Navigate to="/login" replace />;
  }

  // 4. If they are authenticated, render the child route component.
  // The <Outlet /> component is a placeholder provided by react-router-dom
  // where the nested child route's element will be rendered.
  return <Outlet />;
};

export default ProtectedRoute;