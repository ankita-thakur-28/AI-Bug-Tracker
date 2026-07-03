import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { createBug, getTestResult } from '../../services/bugs';
import { getUsers } from '../../services/users';
import { useToast } from '../../hooks/useToast';
import styles from './SubmitBugModal.module.css';

export default function SubmitBugModal({ onClose }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState('HIGH');
  const [assignedToId, setAssignedToId] = useState('');
  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [bugId, setBugId] = useState(null);
  const [testStatus, setTestStatus] = useState(null);
  const { showToast } = useToast();

  useEffect(() => {
    getUsers().then(res => {
      setDevelopers(res.data.filter(u => u.role === 'DEVELOPER'));
    }).catch(() => showToast('Failed to load developers', 'error'));
  }, []);

  useEffect(() => {
    if (!bugId || testStatus === 'PASS' || testStatus === 'FAIL' || testStatus === 'AI_FAILED') return;
    const interval = setInterval(async () => {
      try {
        const res = await getTestResult(bugId);
        setTestStatus(res.data.status);
        if (res.data.status === 'PASS' || res.data.status === 'FAIL' || res.data.status === 'AI_FAILED') {
          setGenerating(false);
          clearInterval(interval);
        }
      } catch { /* ignore polling errors */ }
    }, 2000);
    return () => clearInterval(interval);
  }, [bugId, testStatus]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title || !description || !assignedToId) {
      showToast('All fields required', 'error');
      return;
    }
    setLoading(true);
    try {
      const res = await createBug({ title, description, severity, assignedToId });
      setBugId(res.data.id);
      setSubmitted(true);
      setGenerating(true);
      showToast('Bug submitted! AI generating test...', 'success');
    } catch (err) {
      showToast(err.response?.data?.error || 'Creation failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (submitted) {
    return (
      <Modal onClose={onClose}>
        <div className={styles.submitted}>
          <div className={styles.spinner}></div>
          <h3>Bug Submitted Successfully</h3>
          <p>AI is generating a Playwright test script...</p>
          {generating && <p className={styles.generating}>Status: {testStatus || 'Generating...'}</p>}
          {testStatus && (
            <div className={styles.resultBadge}>
              Test Result: <strong style={{ color: testStatus === 'PASS' ? '#4ade80' : '#f87171' }}>{testStatus}</strong>
            </div>
          )}
          <button className={styles.doneBtn} onClick={onClose}>Done</button>
        </div>
      </Modal>
    );
  }

  return (
    <Modal onClose={onClose}>
      <div className={styles.header}>
        <h3><i className="fas fa-plus" style={{ color: 'var(--accent-cyan)' }}></i> Submit Bug</h3>
        <button className={styles.close} onClick={onClose}>&times;</button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label>Title</label>
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="Brief bug title" />
        </div>
        <div className={styles.formGroup}>
          <label>Description (plain English)</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} required rows={4} placeholder="Describe what happens and what should happen..." />
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
            {loading ? 'Submitting...' : 'Submit Bug'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
