import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      if (status === 401 || status === 403) {
        const fallbackMessage = status === 401
          ? 'You are not authenticated. Please sign in to continue.'
          : 'You do not have permission to perform this action.';
        if (!error.response?.data?.message) {
          if (error.response) {
            error.response.data = { message: fallbackMessage };
          }
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
