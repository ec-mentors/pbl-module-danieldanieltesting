import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
});


apiClient.interceptors.request.use(
  (config) => {

    const token = useAuthStore.getState().token;
    if (token) {

      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;