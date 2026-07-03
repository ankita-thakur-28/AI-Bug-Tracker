import { useState, useEffect } from 'react';
import { getBugs, deleteBug } from '../../services/bugs';
import { useToast } from '../../hooks/useToast';
import EditBugModal from './EditBugModal';
import Loader from '../common/Loader';
import styles from './BugList.module.css';

export default function BugList({ onAdd }) {
  const [bugs, setBugs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [search, setSearch] = useState('');
  const [sortKey, setSortKey] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [editingBug, setEditingBug] = useState(null);
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

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Delete bug "${title}"?`)) return;
    try {
      await deleteBug(id);
      showToast('Bug deleted', 'success');
      fetchBugs();
    } catch {
      showToast('Delete failed', 'error');
    }
  };

  const handleSort = (key) => {
    if (sortKey === key) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  };

  const filtered = bugs
    .filter(b => filter === 'all' || b.status === filter)
    .filter(b => b.title.toLowerCase().includes(search.toLowerCase()) || b.id.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => {
      const aVal = a[sortKey] || '';
      const bVal = b[sortKey] || '';
      const cmp = typeof aVal === 'string' ? aVal.localeCompare(bVal) : aVal > bVal ? 1 : -1;
      return sortDir === 'asc' ? cmp : -cmp;
    });

  if (loading) return <Loader />;

  return (
    <div className={styles.wrap}>
      <div className={styles.toolbar}>
        <div className={styles.filterGroup}>
          {['all', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(f => (
            <button
              key={f}
              className={`${styles.pill} ${filter === f ? styles.active : ''}`}
              onClick={() => setFilter(f)}
            >
              {f === 'all' ? 'All' : f.replace('_', ' ')}
            </button>
          ))}
        </div>
        <div className={styles.searchBox}>
          <i className="fas fa-search"></i>
          <input type="text" placeholder="Search bugs..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
        <button className={styles.addBtn} onClick={onAdd}><i className="fas fa-plus"></i> New Bug</button>
      </div>
      <div className={styles.tableWrap}>
        <table>
          <thead>
            <tr>
              <th onClick={() => handleSort('title')}>Title</th>
              <th onClick={() => handleSort('severity')}>Severity</th>
              <th onClick={() => handleSort('status')}>Status</th>
              <th onClick={() => handleSort('assignedToName')}>Assigned</th>
              <th onClick={() => handleSort('createdAt')}>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(b => (
              <tr key={b.id}>
                <td className={styles.titleCell}>{b.title}</td>
                <td><span className={`badge sev-${b.severity.toLowerCase()}`}>{b.severity}</span></td>
                <td><span className={`badge status-${b.status.toLowerCase()}`}>{b.status.replace('_', ' ')}</span></td>
                <td>{b.assignedToName}</td>
                <td>{new Date(b.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className={styles.actions}>
                    <button onClick={() => setEditingBug(b)} title="Edit"><i className="fas fa-pen"></i></button>
                    <button className={styles.danger} onClick={() => handleDelete(b.id, b.title)} title="Delete"><i className="fas fa-trash"></i></button>
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
      {editingBug && <EditBugModal bug={editingBug} onClose={() => { setEditingBug(null); fetchBugs(); }} />}
    </div>
  );
}
