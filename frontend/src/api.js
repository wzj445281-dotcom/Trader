import axios from 'axios';
const api = axios.create({ baseURL: 'http://localhost:8080/api', withCredentials: true });

window.api = api; // expose for simple calls in components

// Request interceptor: attach token
api.interceptors.request.use(cfg => {
  const t = localStorage.getItem('trader_token');
  if (t) cfg.headers['Authorization'] = 'Bearer ' + t;
  return cfg;
});

// Response interceptor: on 401, try refresh flow once
let isRefreshing = false;
let failedQueue = [];
function processQueue(error, token = null) {
  failedQueue.forEach(p => (error ? p.reject(error) : p.resolve(token)));
  failedQueue = [];
}

api.interceptors.response.use(response => response, async error => {
  const originalReq = error.config;
  if (error.response && error.response.status === 401 && !originalReq._retry) {
    originalReq._retry = true;
    const refresh = localStorage.getItem('trader_refresh');
    if (!refresh) return Promise.reject(error);
    if (isRefreshing) {
      return new Promise(function(resolve, reject) {
        failedQueue.push({ resolve, reject });
      }).then(token => {
        originalReq.headers['Authorization'] = 'Bearer ' + token;
        return api(originalReq);
      }).catch(err => Promise.reject(err));
    }
    isRefreshing = true;
    try {
      const res = await api.post('/auth/refresh', { refresh });
      const newToken = res.data.data.token;
      localStorage.setItem('trader_token', newToken);
      processQueue(null, newToken);
      originalReq.headers['Authorization'] = 'Bearer ' + newToken;
      return api(originalReq);
    } catch (e) {
      processQueue(e, null);
      // clear storage and redirect to login
      localStorage.removeItem('trader_token');
      localStorage.removeItem('trader_refresh');
      localStorage.removeItem('trader_user');
      window.location.href = '/login';
      return Promise.reject(e);
    } finally {
      isRefreshing = false;
    }
  }
  return Promise.reject(error);
});

export const login = d => api.post('/auth/login', d);
export const register = d => api.post('/auth/register', d);
export const list = p => api.get('/prod/list', { params: p });
export const detail = id => api.get('/prod/' + id);
export const uploadImg = fd => api.post('/prod/uploadImg', fd, { headers: {'Content-Type':'multipart/form-data'}});
export const publish = d => api.post('/prod/publish', d);
export const fav = d => api.post('/prod/fav', d);
export const favs = id => api.get('/prod/favs/' + id);
export const refreshToken = (refresh) => api.post('/auth/refresh', { refresh });
export const logoutApi = (refresh) => api.post('/auth/logout', { refresh });

export default api;

export const getCart = (userId) => api.get('/prod/cart/' + userId);
