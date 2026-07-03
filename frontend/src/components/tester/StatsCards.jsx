import { useState, useEffect } from 'react';
import { getBugs } from '../../services/bugs';
import Loader from '../common/Loader';
import styles from './StatsCards.module.css';

export default function StatsCards() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const res = await getBugs();
      const bugs = res.data;
      const total = bugs.length;
      const open = bugs.filter(b => b.status === 'OPEN' || b.status === 'IN_PROGRESS').length;
      const resolved = bugs.filter(b => b.status === 'RESOLVED' || b.status === 'CLOSED').length;
      setStats({ total, open, resolved });
    } catch {
      setStats({ total: 0, open: 0, resolved: 0 });
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loader />;

  return (
    <div className={styles.grid}>
      <div className={styles.card}>
        <div className={styles.icon} style={{ background: 'rgba(124,108,248,0.15)', color: 'var(--accent-purple)' }}>
          <i className="fas fa-bug"></i>
        </div>
        <div className={styles.info}>
          <div className={styles.value}>{stats.total}</div>
          <div className={styles.label}>Total Filed</div>
        </div>
      </div>
      <div className={styles.card}>
        <div className={styles.icon} style={{ background: 'rgba(56,189,248,0.15)', color: '#38bdf8' }}>
          <i className="fas fa-hourglass-half"></i>
        </div>
        <div className={styles.info}>
          <div className={styles.value}>{stats.open}</div>
          <div className={styles.label}>Open / In Progress</div>
        </div>
      </div>
      <div className={styles.card}>
        <div className={styles.icon} style={{ background: 'rgba(74,222,128,0.15)', color: '#4ade80' }}>
          <i className="fas fa-check-circle"></i>
        </div>
        <div className={styles.info}>
          <div className={styles.value}>{stats.resolved}</div>
          <div className={styles.label}>Resolved / Closed</div>
        </div>
      </div>
    </div>
  );
}
