import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

const isUsableToken = (value: string | null): value is string => {
  if (!value) {
    return false;
  }
  const normalized = value.trim().toLowerCase();
  return normalized.length > 0 && normalized !== 'null' && normalized !== 'undefined';
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  config.headers = config.headers ?? {};
  if (isUsableToken(token)) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  } else {
    delete config.headers.Authorization;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      if (status === 401) {
        const clearAuth = (window as { __clearSportsMsAuth?: () => void }).__clearSportsMsAuth;
        clearAuth?.();
        if (window.location.pathname !== '/login') {
          window.location.assign('/login?reason=session-expired');
        }
      }
      if (status === 401 || status === 403) {
        const fallbackMessage = status === 401 ? 'Unauthenticated' : 'Forbidden';
        if (!error.response?.data?.message && error.response) {
          error.response.data = { message: fallbackMessage };
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
