import axios from 'axios';

const axiosClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

axiosClient.interceptors.request.use((config) => {
  const businessUnitId = localStorage.getItem('active_bu_id') || '00000000-0000-0000-0000-000000000000';
  const token = localStorage.getItem('auth_token');

  config.headers = {
    ...config.headers,
    'X-BU-ID': businessUnitId,
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };

  return config;
});

export default axiosClient;
