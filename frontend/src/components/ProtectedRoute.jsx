import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

const ProtectedRoute = () => {
  const { isAuthenticated } = useContext(AuthContext);

  if (!isAuthenticated) {

    return <Navigate to="/login" replace />;
  }


  return <Outlet />;
};

export default ProtectedRoute;