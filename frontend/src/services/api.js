import axios from 'axios';

// Create an Axios instance with a predefined configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // The base URL for our Spring Boot backend
  headers: {
    'Content-Type': 'application/json',
  },
});

// --- IMPORTANT: The Request Interceptor ---
// This function will run before every single request is sent by this Axios instance.
api.interceptors.request.use(
  (config) => {
    // Retrieve the user's token from localStorage
    const userToken = localStorage.getItem('token');

    // If the token exists, attach it to the Authorization header
    if (userToken) {
      config.headers['Authorization'] = `Bearer ${userToken}`;
    }

    // Must return the config object, otherwise the request will be blocked
    return config;
  },
  (error) => {
    // Handle any errors that occur during the request setup
    return Promise.reject(error);
  }
);

export default api;