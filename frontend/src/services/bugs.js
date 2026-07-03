import api from './api';

export const getBugs = () => api.get('/bugs');

export const getBug = (id) => api.get(`/bugs/${id}`);

export const createBug = (data) => api.post('/bugs', data);

export const updateBug = (id, data) => api.put(`/bugs/${id}`, data);

export const deleteBug = (id) => api.delete(`/bugs/${id}`);

export const updateBugStatus = (id, status) => api.patch(`/bugs/${id}/status`, { status });

export const cancelBug = (id) => api.patch(`/bugs/${id}/cancel`);

export const getTestResult = (id) => api.get(`/bugs/${id}/test-result`);
