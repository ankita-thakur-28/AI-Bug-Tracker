import api from './api';
import { setToken } from './tokenManager';

export const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password });
  const { token, email: userEmail, role, name } = res.data;
  setToken(token);
  return { user: { email: userEmail, role, name } };
};

export const signup = async (name, email, password) => {
  const res = await api.post('/auth/signup', { name, email, password });
  return res.data;
};

export const logout = async () => {
  try {
    await api.post('/auth/logout');
  } catch {
    // ignore logout errors
  }
  setToken(null);
};
