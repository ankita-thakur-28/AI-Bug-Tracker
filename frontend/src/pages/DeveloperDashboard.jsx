import Sidebar from '../components/layout/Sidebar';
import Topbar from '../components/layout/Topbar';
import AssignedBugsList from '../components/developer/AssignedBugsList';
import styles from './Dashboard.module.css';

export default function DeveloperDashboard() {
  return (
    <div className={styles.dashboard}>
      <Sidebar role="DEVELOPER" activeTab="bugs" setActiveTab={() => {}} />
      <div className={styles.mainArea}>
        <Topbar title="My Assigned Bugs" />
        <div className={styles.content}>
          <AssignedBugsList />
        </div>
      </div>
    </div>
  );
}
