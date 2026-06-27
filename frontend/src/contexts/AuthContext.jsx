import { createContext, useState, useContext, useCallback } from 'react';
import { login as apiLogin, signup as apiSignup, logout as apiLogout } from '../services/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const login = useCallback(async (email, password) => {
    try {
      const { user: userData } = await apiLogin(email, password);
      setUser(userData);
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.error || err.message || 'Login failed';
      return { success: false, error: msg };
    }
  }, []);

  const signup = useCallback(async (name, email, password) => {
    try {
      await apiSignup(name, email, password);
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.error || err.message || 'Signup failed';
      return { success: false, error: msg };
    }
  }, []);

  const logout = useCallback(async () => {
    await apiLogout();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
