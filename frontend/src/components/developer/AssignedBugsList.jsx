import { useState, useEffect } from 'react';
import { getBugs, updateBugStatus } from '../../services/bugs';
import { useToast } from '../../hooks/useToast';
import BugDetailModal from './BugDetailModal';
import Loader from '../common/Loader';
import styles from './AssignedBugsList.module.css';

const STATUS_OPTIONS = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

export default function AssignedBugsList() {
  const [bugs, setBugs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedBug, setSelectedBug] = useState(null);
  const [confirmStatus, setConfirmStatus] = useState(null);
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

  const handleStatusChange = async (bugId, newStatus) => {
    try {
      await updateBugStatus(bugId, newStatus);
      showToast(`Status updated to ${newStatus.replace('_', ' ')}`, 'success');
      setConfirmStatus(null);
      fetchBugs();
    } catch {
      showToast('Status update failed', 'error');
    }
  };

  if (loading) return <Loader />;

  return (
    <div className={styles.wrap}>
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
            {bugs.map(b => (
              <tr key={b.id}>
                <td className={styles.titleCell}>{b.title}</td>
                <td><span className={`badge sev-${b.severity.toLowerCase()}`}>{b.severity}</span></td>
                <td>
                  <div className={styles.statusWrapper}>
                    <span className={`badge status-${b.status.toLowerCase()}`}>{b.status.replace('_', ' ')}</span>
                  </div>
                </td>
                <td><span className={`badge ${b.testStatus === 'PASS' ? 'sev-low' : 'sev-critical'}`}>{b.testStatus || 'PENDING'}</span></td>
                <td>{new Date(b.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className={styles.actions}>
                    <button className={styles.viewBtn} onClick={() => setSelectedBug(b)} title="View Details">
                      <i className="fas fa-eye"></i>
                    </button>
                    <div className={styles.statusDropdown}>
                      <select
                        value=""
                        onChange={(e) => {
                          if (e.target.value) {
                            setConfirmStatus({ bugId: b.id, newStatus: e.target.value, title: b.title });
                          }
                        }}
                      >
                        <option value="">Status...</option>
                        {STATUS_OPTIONS.filter(s => s !== b.status).map(s => (
                          <option key={s} value={s}>{s.replace('_', ' ')}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                </td>
              </tr>
            ))}
            {bugs.length === 0 && (
              <tr><td colSpan={6} className="text-center text-muted" style={{ padding: 40 }}>No bugs assigned to you</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {confirmStatus && (
        <div className={styles.confirmOverlay} onClick={() => setConfirmStatus(null)}>
          <div className={styles.confirmBox} onClick={e => e.stopPropagation()}>
            <h3>Change Status</h3>
            <p>Update <strong>{confirmStatus.title}</strong> to <strong>{confirmStatus.newStatus.replace('_', ' ')}</strong>?</p>
            <div className={styles.confirmActions}>
              <button className={styles.cancelBtn} onClick={() => setConfirmStatus(null)}>Cancel</button>
              <button className={styles.confirmBtn} onClick={() => handleStatusChange(confirmStatus.bugId, confirmStatus.newStatus)}>
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedBug && (
        <BugDetailModal bug={selectedBug} onClose={() => setSelectedBug(null)} />
      )}
    </div>
  );
}
