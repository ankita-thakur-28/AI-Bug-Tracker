import { createContext, useState, useCallback } from 'react';
import styles from './Toast.module.css';

export const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, type = 'info') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter(t => t.id !== id));
    }, 3500);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className={styles.container}>
        {toasts.map(({ id, message, type }) => (
          <div key={id} className={`${styles.toast} ${styles[type]}`}>
            <i className={`fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}`} />
            {message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
