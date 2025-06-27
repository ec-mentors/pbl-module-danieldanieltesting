import React, { useEffect, useContext } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';

const OAuth2RedirectHandler = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithToken } = useContext(AuthContext);

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (token) {
      loginWithToken(token);
      navigate('/');
    } else if (error) {
      toast.error(`Login failed: ${error}`);
      navigate('/login');
    } else {
      toast.error('An unknown error occurred during login.');
      navigate('/login');
    }
  }, [searchParams, navigate, loginWithToken]);

  return <Spinner message="Finalizing login..." />;
};

export default OAuth2RedirectHandler;