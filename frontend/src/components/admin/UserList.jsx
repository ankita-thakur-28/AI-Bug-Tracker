import { useState, useEffect } from 'react';
import { getUsers, updateUser, deleteUser } from '../../services/users';
import { useAuth } from '../../contexts/AuthContext';
import { useToast } from '../../hooks/useToast';
import Modal from '../common/Modal';
import Loader from '../common/Loader';
import styles from './UserList.module.css';

export default function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingUser, setEditingUser] = useState(null);
  const [editEmail, setEditEmail] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const { user: currentUser } = useAuth();
  const { showToast } = useToast();

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await getUsers();
      setUsers(res.data);
    } catch {
      showToast('Failed to load users', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (u) => {
    setEditingUser(u);
    setEditEmail(u.email);
    setEditPhone(u.phone || '');
  };

  const handleSave = async () => {
    try {
      await updateUser(editingUser.id, { email: editEmail, phone: editPhone });
      showToast('User updated', 'success');
      setEditingUser(null);
      fetchUsers();
    } catch (err) {
      showToast(err.response?.data?.error || 'Update failed', 'error');
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete user "${name}"?`)) return;
    try {
      await deleteUser(id);
      showToast('User deleted', 'success');
      fetchUsers();
    } catch {
      showToast('Delete failed', 'error');
    }
  };

  if (loading) return <Loader />;

  return (
    <div className={styles.wrap}>
      <div className={styles.toolbar}>
        <span className={styles.count}>{users.length} users</span>
      </div>
      <div className={styles.tableWrap}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Phone</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id}>
                <td className={styles.nameCell}>{u.name}</td>
                <td>{u.email}</td>
                <td><span className={`badge role-${u.role.toLowerCase()}`}>{u.role}</span></td>
                <td>{u.phone || '-'}</td>
                <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className={styles.actions}>
                    <button onClick={() => handleEdit(u)} title="Edit"><i className="fas fa-pen"></i></button>
                    <button
                      className={styles.danger}
                      onClick={() => handleDelete(u.id, u.name)}
                      disabled={u.id === currentUser?.id}
                      title={u.id === currentUser?.id ? 'Cannot delete yourself' : 'Delete'}
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {editingUser && (
        <Modal onClose={() => setEditingUser(null)}>
          <div style={{ marginBottom: 24 }}>
            <h3 style={{ fontSize: 18, fontWeight: 600 }}><i className="fas fa-pen" style={{ color: 'var(--accent-cyan)', marginRight: 10 }}></i>Edit User</h3>
          </div>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 5 }}>Email</label>
            <input
              type="email"
              value={editEmail}
              onChange={(e) => setEditEmail(e.target.value)}
              style={{ width: '100%', padding: '10px 14px', background: 'var(--bg-input)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', color: 'var(--text-primary)', fontSize: 14, fontFamily: 'var(--font)', outline: 'none' }}
            />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 5 }}>Phone</label>
            <input
              type="text"
              value={editPhone}
              onChange={(e) => setEditPhone(e.target.value)}
              style={{ width: '100%', padding: '10px 14px', background: 'var(--bg-input)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', color: 'var(--text-primary)', fontSize: 14, fontFamily: 'var(--font)', outline: 'none' }}
            />
          </div>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 24 }}>
            <button onClick={() => setEditingUser(null)} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', fontSize: 14, fontWeight: 600, fontFamily: 'var(--font)', cursor: 'pointer', background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>Cancel</button>
            <button onClick={handleSave} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', fontSize: 14, fontWeight: 600, fontFamily: 'var(--font)', cursor: 'pointer', background: 'var(--accent-gradient)', border: 'none', color: '#fff' }}>Save</button>
          </div>
        </Modal>
      )}
    </div>
  );
}
