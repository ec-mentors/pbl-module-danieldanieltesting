// src/services/apiClient.ts
import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Your backend API base URL
});

// Request Interceptor: This runs BEFORE each request is sent.
apiClient.interceptors.request.use(
  (config) => {
    // Get the token from our state management store
    const token = useAuthStore.getState().token;
    if (token) {
      // If the token exists, add it to the Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;