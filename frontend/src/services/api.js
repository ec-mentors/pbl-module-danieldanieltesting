import axios from 'axios';


const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

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

export const getPrompts = (params) => api.get('/prompts', { params });
export const getPromptById = (promptId) => api.get(`/prompts/${promptId}`);
export const createPrompt = (promptData) => api.post('/prompts', promptData);
export const updatePrompt = (promptId, promptData) => api.put(`/prompts/${promptId}`, promptData);
export const deletePrompt = (promptId) => api.delete(`/prompts/${promptId}`);


export const getTags = () => api.get('/tags');
export const updatePromptTags = (promptId, tagNames) => api.post(`/prompts/${promptId}/tags`, tagNames);

export const getCollections = () => api.get('/collections');
export const getCollectionById = (collectionId) => api.get(`/collections/${collectionId}`);
export const createCollection = (collectionData) => api.post('/collections', collectionData);
export const updateCollection = (collectionId, collectionData) => api.put(`/collections/${collectionId}`, collectionData);
export const deleteCollection = (collectionId) => api.delete(`/collections/${collectionId}`);
export const addPromptToCollection = (collectionId, promptId) => api.put(`/collections/${collectionId}/prompts/${promptId}`);
export const removePromptFromCollection = (collectionId, promptId) => api.delete(`/collections/${collectionId}/prompts/${promptId}`);

export const createReview = (promptId, reviewData) => api.post(`/prompts/${promptId}/reviews`, reviewData);
export const deleteReview = (reviewId) => api.delete(`/reviews/${reviewId}`);

export const getUserProfile = (username) => api.get(`/users/${username}/profile`);
export const getPromptsByUsername = (username, params) => api.get(`/users/${username}/prompts`, { params });
export const followUser = (username) => api.post(`/users/${username}/follow`);
export const unfollowUser = (username) => api.post(`/users/${username}/unfollow`);

export const getFeed = (params) => api.get('/feed', { params });


export default api;