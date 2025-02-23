import axios from 'axios';

const URL = process.env.NODE_ENV === 'production' ?  window.__api_base_url__: 'http://localhost:5000';

const axiosInstance = axios.create({
  baseURL: URL,
});

export default axiosInstance;

