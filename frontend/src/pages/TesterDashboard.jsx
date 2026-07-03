import { useState } from 'react';
import Sidebar from '../components/layout/Sidebar';
import Topbar from '../components/layout/Topbar';
import StatsCards from '../components/tester/StatsCards';
import MyBugsList from '../components/tester/MyBugsList';
import SubmitBugModal from '../components/tester/SubmitBugModal';
import ChangePasswordModal from '../components/tester/ChangePasswordModal';
import styles from './Dashboard.module.css';

export default function TesterDashboard() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [showSubmit, setShowSubmit] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className={styles.dashboard}>
      <Sidebar role="TESTER" activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className={styles.mainArea}>
        <Topbar
          title={
            activeTab === 'dashboard' ? 'Dashboard' :
            activeTab === 'bugs' ? 'My Bugs' : 'Test Results'
          }
          actions={
            <>
              <button
                className="btn-primary-sm"
                onClick={() => setShowSubmit(true)}
                style={{ padding: '8px 18px', borderRadius: 'var(--radius-sm)', background: 'var(--accent-gradient)', border: 'none', color: '#fff', fontSize: 13, fontWeight: 600, fontFamily: 'var(--font)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}
              >
                <i className="fas fa-plus"></i> Submit Bug
              </button>
              <button
                onClick={() => setShowPassword(true)}
                style={{ padding: '8px 14px', borderRadius: 'var(--radius-sm)', background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-secondary)', fontSize: 13, fontWeight: 500, fontFamily: 'var(--font)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}
              >
                <i className="fas fa-key"></i> Password
              </button>
            </>
          }
        />
        <div className={styles.content}>
          {activeTab === 'dashboard' && <StatsCards />}
          {activeTab === 'bugs' && <MyBugsList />}
        </div>
      </div>
      {showSubmit && <SubmitBugModal onClose={() => setShowSubmit(false)} />}
      {showPassword && <ChangePasswordModal onClose={() => setShowPassword(false)} />}
    </div>
  );
}
