import { useAuth } from '../../contexts/AuthContext';
import styles from './Sidebar.module.css';

export default function Sidebar({ role, activeTab, setActiveTab }) {
  const { user, logout } = useAuth();

  const navItems = role === 'ADMIN'
    ? [{ id: 'bugs', label: 'Bugs', icon: 'fa-list' }, { id: 'users', label: 'Manage Users', icon: 'fa-users' }]
    : role === 'DEVELOPER'
      ? [{ id: 'bugs', label: 'My Bugs', icon: 'fa-tasks' }]
      : role === 'TESTER'
        ? [{ id: 'dashboard', label: 'Dashboard', icon: 'fa-chart-pie' }, { id: 'bugs', label: 'My Bugs', icon: 'fa-bug' }]
        : [];

  const initials = user?.name?.split(' ').map(w => w[0]).join('') || 'U';

  return (
    <aside className={styles.sidebar}>
      <div className={styles.brand}>
        <div className={styles.icon}><i className="fas fa-bug"></i></div>
        <h2>AI-Bug Tracker</h2>
      </div>
      <nav className={styles.nav}>
        {navItems.map(item => (
          <button
            key={item.id}
            className={`${styles.navItem} ${activeTab === item.id ? styles.active : ''}`}
            onClick={() => setActiveTab(item.id)}
            aria-label={item.label}
            aria-current={activeTab === item.id ? 'page' : undefined}
          >
            <i className={`fas ${item.icon}`} aria-hidden="true"></i> {item.label}
          </button>
        ))}
        <button className={`${styles.navItem} ${styles.logout}`} onClick={logout} aria-label="Logout">
          <i className="fas fa-sign-out-alt"></i> Logout
        </button>
      </nav>
      <div className={styles.userChip}>
        <div className={styles.avatar}>{initials}</div>
        <div className={styles.info}>
          <div className={styles.name}>{user?.name}</div>
          <div className={styles.role}>{user?.role}</div>
        </div>
      </div>
    </aside>
  );
}
