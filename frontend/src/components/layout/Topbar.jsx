import { useAuth } from '../../contexts/AuthContext';
import styles from './Topbar.module.css';

export default function Topbar({ title, actions }) {
  const { user } = useAuth();
  const initials = user?.name?.split(' ').map(w => w[0]).join('') || 'U';

  return (
    <header className={styles.topbar}>
      <div className={styles.title}>{title}</div>
      <div className={styles.actions}>
        {actions}
        <div className={styles.avatar}>{initials}</div>
      </div>
    </header>
  );
}
