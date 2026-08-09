import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { createBug } from '../../services/bugs';
import { getUsers } from '../../services/users';
import { useToast } from '../../hooks/useToast';
import styles from './BugModal.module.css';

export default function AddBugModal({ onClose }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState('HIGH');
  const [assignedToId, setAssignedToId] = useState('AUTO');
  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    getUsers().then(res => {
      setDevelopers(res.data.filter(u => u.role === 'DEVELOPER'));
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
      const payload = {
        title,
        description,
        severity,
        assignedToId: assignedToId === 'AUTO' ? null : assignedToId
      };
      await createBug(payload);
      showToast('Bug created successfully', 'success');
      onClose();
    } catch (err) {
      showToast(err.response?.data?.error || 'Creation failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <div className={styles.header}>
        <h3><i className="fas fa-plus" style={{ color: 'var(--accent-cyan)' }}></i> New Bug</h3>
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
            <option value="AUTO">✨ Auto-Assign (AI Recommended)</option>
            {developers.map(d => (
              <option key={d.id} value={d.id}>{d.name} ({d.email})</option>
            ))}
          </select>
        </div>
        <div className={styles.actions}>
          <button type="button" className={styles.btnSecondary} onClick={onClose}>Cancel</button>
          <button type="submit" className={styles.btnPrimary} disabled={loading}>
            {loading ? 'Creating...' : 'Create Bug'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
