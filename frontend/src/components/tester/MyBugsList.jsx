import { useState, useEffect } from 'react';
import { getBugs, cancelBug } from '../../services/bugs';
import { useToast } from '../../hooks/useToast';
import Loader from '../common/Loader';
import styles from './MyBugsList.module.css';

export default function MyBugsList() {
  const [bugs, setBugs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const { showToast } = useToast();

  useEffect(() => { fetchBugs(); }, []);

  const fetchBugs = async () => {
    setLoading(true);
    try {
      const res = await getBugs();
      setBugs(res.data);
    } catch {
      showToast('Failed to load bugs', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id, title) => {
    if (!window.confirm(`Withdraw bug "${title}"?`)) return;
    try {
      await cancelBug(id);
      showToast('Bug withdrawn', 'success');
      fetchBugs();
    } catch {
      showToast('Cancel failed', 'error');
    }
  };

  const filtered = filter === 'all'
    ? bugs
    : bugs.filter(b => b.status === filter);

  if (loading) return <Loader />;

  return (
    <div className={styles.wrap}>
      <div className={styles.toolbar}>
        <div className={styles.filterGroup}>
          {['all', 'OPEN', 'IN_PROGRESS', 'RESOLVED'].map(f => (
            <button
              key={f}
              className={`${styles.pill} ${filter === f ? styles.active : ''}`}
              onClick={() => setFilter(f)}
            >
              {f === 'all' ? 'All' : f.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>
      <div className={styles.tableWrap}>
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Severity</th>
              <th>Status</th>
              <th>Test Result</th>
              <th>Filed</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(b => (
              <tr key={b.id}>
                <td className={styles.titleCell}>{b.title}</td>
                <td><span className={`badge sev-${b.severity.toLowerCase()}`}>{b.severity}</span></td>
                <td><span className={`badge status-${b.status.toLowerCase()}`}>{b.status.replace('_', ' ')}</span></td>
                <td><span className={`badge ${b.testStatus === 'PASS' ? 'sev-low' : 'sev-critical'}`}>{b.testStatus || 'PENDING'}</span></td>
                <td>{new Date(b.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className={styles.actions}>
                    <button className={styles.viewBtn} title="View"><i className="fas fa-eye"></i></button>
                    {b.status !== 'RESOLVED' && b.status !== 'CLOSED' && b.status !== 'WITHDRAWN' && (
                      <button className={styles.cancelBtn} onClick={() => handleCancel(b.id, b.title)} title="Cancel">
                        <i className="fas fa-ban"></i>
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={6} className="text-center text-muted" style={{ padding: 40 }}>No bugs found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
