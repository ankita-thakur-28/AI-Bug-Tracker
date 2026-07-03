import api from './api';

export const getUsers = () => api.get('/users');

export const updateUser = (id, data) => api.put(`/users/${id}`, data);

export const deleteUser = (id) => api.delete(`/users/${id}`);

export const changePassword = (data) => api.patch('/users/password', data);
