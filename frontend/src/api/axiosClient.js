import axios from 'axios';
import toast from 'react-hot-toast';
import { API_BASE_URL, DEFAULT_TENANT_ID, STORAGE_KEYS } from '../constants/config';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request Interceptor: Inject JWT Bearer & Tenant ID
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const tenantId = localStorage.getItem(STORAGE_KEYS.TENANT_ID) || DEFAULT_TENANT_ID;
    config.headers['X-Tenant-ID'] = tenantId;

    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401, 403, 409, 429, 500 & Offline errors
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      toast.error('Network Error: Unable to connect to server. Check connection.');
      return Promise.reject(error);
    }

    const status = error.response.status;

    if (status === 401) {
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      toast.error('Session expired. Please log in again.');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    } else if (status === 403) {
      toast.error('Access Denied: You do not have permission for this resource.');
    } else if (status === 409) {
      toast.error('Conflict Error: Resource state conflict detected.');
    } else if (status === 429) {
      toast.error('Rate Limit Exceeded: Too many requests. Please slow down.');
    } else if (status >= 500) {
      toast.error('Server Error: Internal system error. Please try again.');
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
