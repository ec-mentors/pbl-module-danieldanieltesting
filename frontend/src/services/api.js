// src/services/api.js
import axios from 'axios';

// Create an Axios instance with a predefined configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// --- Request Interceptor ---
api.interceptors.request.use(
  (config) => {
    const userToken = localStorage.getItem('token');
    if (userToken) {
      config.headers['Authorization'] = `Bearer ${userToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// === PROMPT API FUNCTIONS ===
export const getPrompts = (params) => api.get('/prompts', { params });
export const getPromptById = (promptId) => api.get(`/prompts/${promptId}`);
export const createPrompt = (promptData) => api.post('/prompts', promptData);
export const updatePrompt = (promptId, promptData) => api.put(`/prompts/${promptId}`, promptData);
export const deletePrompt = (promptId) => api.delete(`/prompts/${promptId}`);

// === TAG API FUNCTIONS ===
export const getTags = () => api.get('/tags');
export const updatePromptTags = (promptId, tagNames) => api.post(`/prompts/${promptId}/tags`, tagNames);

// === COLLECTION API FUNCTIONS ===
export const getCollections = () => api.get('/collections');
export const getCollectionById = (collectionId) => api.get(`/collections/${collectionId}`);
export const createCollection = (collectionData) => api.post('/collections', collectionData);
export const updateCollection = (collectionId, collectionData) => api.put(`/collections/${collectionId}`, collectionData);
export const deleteCollection = (collectionId) => api.delete(`/collections/${collectionId}`);
export const addPromptToCollection = (collectionId, promptId) => api.post(`/collections/${collectionId}/prompts/${promptId}`);
export const removePromptFromCollection = (collectionId, promptId) => api.delete(`/collections/${collectionId}/prompts/${promptId}`);

// === NEW/UPDATED REVIEW API FUNCTIONS ===
// POST to /prompts/{promptId}/reviews
export const createReview = (promptId, reviewData) => api.post(`/prompts/${promptId}/reviews`, reviewData);
// DELETE to /reviews/{reviewId}
export const deleteReview = (reviewId) => api.delete(`/reviews/${reviewId}`);

export default api;