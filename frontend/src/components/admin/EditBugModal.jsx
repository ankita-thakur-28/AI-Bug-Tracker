import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { updateBug } from '../../services/bugs';
import { getUsers } from '../../services/users';
import { useToast } from '../../hooks/useToast';
import styles from './BugModal.module.css';

export default function EditBugModal({ bug, onClose }) {
  const [title, setTitle] = useState(bug.title || '');
  const [description, setDescription] = useState(bug.description || '');
  const [severity, setSeverity] = useState(bug.severity || 'HIGH');
  const [assignedToId, setAssignedToId] = useState('');
  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    getUsers().then(res => {
      const devs = res.data.filter(u => u.role === 'DEVELOPER');
      setDevelopers(devs);
      const current = devs.find(d => d.name === bug.assignedToName);
      setAssignedToId(current ? current.id : '');
    }).catch(() => showToast('Failed to load developers', 'error'));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title || !description || !assignedToId) {
      showToast('All fields required', 'error');
      return;
    }
    setLoading(true);
    try {
      await updateBug(bug.id, { title, description, severity, assignedToId });
      showToast('Bug updated', 'success');
      onClose();
    } catch (err) {
      showToast(err.response?.data?.error || 'Update failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <div className={styles.header}>
        <h3><i className="fas fa-pen" style={{ color: 'var(--accent-cyan)' }}></i> Edit Bug</h3>
        <button className={styles.close} onClick={onClose}>&times;</button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label>Title</label>
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
        </div>
        <div className={styles.formGroup}>
          <label>Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} required rows={4} />
        </div>
        <div className={styles.formGroup}>
          <label>Severity</label>
          <select value={severity} onChange={(e) => setSeverity(e.target.value)}>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
        <div className={styles.formGroup}>
          <label>Assign to Developer</label>
          <select value={assignedToId} onChange={(e) => setAssignedToId(e.target.value)} required>
            <option value="">-- Select --</option>
            {developers.map(d => (
              <option key={d.id} value={d.id}>{d.name} ({d.email})</option>
            ))}
          </select>
        </div>
        <div className={styles.actions}>
          <button type="button" className={styles.btnSecondary} onClick={onClose}>Cancel</button>
          <button type="submit" className={styles.btnPrimary} disabled={loading}>
            {loading ? 'Updating...' : 'Update Bug'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
