import axios from 'axios';

const axiosClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor: Attach Auth token and Business Unit ID
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    const buId = localStorage.getItem('active_bu_id') || '00000000-0000-0000-0000-000000000000';

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-BU-ID'] = buId;

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Clear session on 401 unauthorized errors
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
