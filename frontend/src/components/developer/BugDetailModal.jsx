import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { getTestResult } from '../../services/bugs';
import styles from './BugDetailModal.module.css';

export default function BugDetailModal({ bug, onClose }) {
  const [testResult, setTestResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const fetchResult = async () => {
      try {
        const res = await getTestResult(bug.id);
        if (!cancelled) {
          setTestResult(res.data);
          setLoading(false);
        }
      } catch {
        if (!cancelled) setLoading(false);
      }
    };
    fetchResult();

    const interval = setInterval(fetchResult, 3000);
    return () => { cancelled = true; clearInterval(interval); };
  }, [bug.id]);

  const handleCopy = () => {
    if (testResult?.code) {
      navigator.clipboard.writeText(testResult.code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const statusBadge = (status) => {
    if (status === 'PASS') return <span className={styles.badgePass}><i className="fas fa-check-circle"></i> PASS</span>;
    if (status === 'FAIL') return <span className={styles.badgeFail}><i className="fas fa-times-circle"></i> FAIL</span>;
    if (status === 'PENDING') return <span className={styles.badgePending}><i className="fas fa-hourglass-half"></i> PENDING</span>;
    if (status === 'AI_FAILED') return <span className={styles.badgeFail}><i className="fas fa-exclamation-triangle"></i> AI FAILED</span>;
    return null;
  };

  return (
    <Modal onClose={onClose}>
      <div className={styles.header}>
        <h3><i className="fas fa-bug" style={{ color: 'var(--accent-cyan)', marginRight: 8 }}></i> {bug.title}</h3>
        <button className={styles.close} onClick={onClose}>&times;</button>
      </div>

      <div className={styles.section}>
        <div className={styles.field}>
          <span className={styles.label}>Severity</span>
          <span className={`badge sev-${bug.severity?.toLowerCase()}`}>{bug.severity}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>Status</span>
          <span className={`badge status-${bug.status?.toLowerCase()}`}>{bug.status?.replace('_', ' ')}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>Assigned to</span>
          <span>{bug.assignedToName}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>Reported by</span>
          <span>{bug.createdByName}</span>
        </div>
      </div>

      <div className={styles.section}>
        <h4>Description</h4>
        <p className={styles.description}>{bug.description}</p>
      </div>

      <div className={styles.section}>
        <div className={styles.testHeader}>
          <h4>AI Generated Test</h4>
          {testResult?.code && (
            <button className={styles.copyBtn} onClick={handleCopy}>
              <i className={`fas ${copied ? 'fa-check' : 'fa-copy'}`}></i> {copied ? 'Copied!' : 'Copy'}
            </button>
          )}
        </div>
        {loading ? (
          <div className={styles.loadingTest}>Loading test script...</div>
        ) : testResult ? (
          <>
            <div className={styles.statusRow}>
              {statusBadge(testResult.status)}
              {testResult.executedAt && (
                <span className={styles.executedAt}>
                  <i className="far fa-clock"></i> {new Date(testResult.executedAt).toLocaleString()}
                </span>
              )}
            </div>
            {testResult.code && (
              <pre className={styles.codeBlock}>
                <code>{testResult.code}</code>
              </pre>
            )}
            {testResult.logs && (
              <div className={styles.logSection}>
                <h5>Execution Logs</h5>
                <pre className={styles.logBlock}>{testResult.logs}</pre>
              </div>
            )}
          </>
        ) : (
          <div className={styles.loadingTest}>No test result available</div>
        )}
      </div>
    </Modal>
  );
}
