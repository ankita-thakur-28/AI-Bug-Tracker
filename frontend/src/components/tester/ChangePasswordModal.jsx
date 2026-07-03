import { useState } from 'react';
import Modal from '../common/Modal';
import { changePassword } from '../../services/users';
import { useToast } from '../../hooks/useToast';
import styles from './SubmitBugModal.module.css';

export default function ChangePasswordModal({ onClose }) {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      showToast('Passwords do not match', 'error');
      return;
    }
    if (newPassword.length < 6) {
      showToast('Password must be at least 6 characters', 'error');
      return;
    }
    setLoading(true);
    try {
      await changePassword({ currentPassword, newPassword });
      showToast('Password changed successfully', 'success');
      onClose();
    } catch (err) {
      showToast(err.response?.data?.error || 'Password change failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <div className={styles.header}>
        <h3><i className="fas fa-key" style={{ color: 'var(--accent-cyan)' }}></i> Change Password</h3>
        <button className={styles.close} onClick={onClose}>&times;</button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label>Current Password</label>
          <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
        </div>
        <div className={styles.formGroup}>
          <label>New Password</label>
          <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required placeholder="At least 6 characters" />
        </div>
        <div className={styles.formGroup}>
          <label>Confirm New Password</label>
          <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
        </div>
        <div className={styles.actions}>
          <button type="button" className={styles.btnSecondary} onClick={onClose}>Cancel</button>
          <button type="submit" className={styles.btnPrimary} disabled={loading}>
            {loading ? 'Changing...' : 'Change Password'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
