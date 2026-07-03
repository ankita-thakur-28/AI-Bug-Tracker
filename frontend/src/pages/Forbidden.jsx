import { Link } from 'react-router-dom';
import styles from './Auth.module.css';

export default function Forbidden() {
  return (
    <div className={styles.authScreen}>
      <div className={styles.authBox}>
        <h2 style={{ color: '#f87171' }}><i className="fas fa-ban"></i> Access Denied</h2>
        <p style={{ margin: '20px 0', color: 'var(--text-secondary)' }}>
          You don&apos;t have permission to view this page.
        </p>
        <Link to="/" className={styles.btnPrimary} style={{ display: 'block', textAlign: 'center' }}>
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
