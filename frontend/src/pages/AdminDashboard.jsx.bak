import { useState } from 'react';
import Sidebar from '../components/layout/Sidebar';
import Topbar from '../components/layout/Topbar';
import BugList from '../components/admin/BugList';
import UserList from '../components/admin/UserList';
import AddBugModal from '../components/admin/AddBugModal';
import styles from './Dashboard.module.css';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('bugs');
  const [showAddBug, setShowAddBug] = useState(false);

  return (
    <div className={styles.dashboard}>
      <Sidebar role="ADMIN" activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className={styles.mainArea}>
        <Topbar title={activeTab === 'bugs' ? 'Bug Management' : 'Manage Users'} />
        <div className={styles.content}>
          {activeTab === 'bugs' && <BugList onAdd={() => setShowAddBug(true)} />}
          {activeTab === 'users' && <UserList />}
        </div>
      </div>
      {showAddBug && <AddBugModal onClose={() => setShowAddBug(false)} />}
    </div>
  );
}
