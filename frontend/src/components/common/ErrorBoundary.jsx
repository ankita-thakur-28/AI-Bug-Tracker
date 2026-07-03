import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null, info: null };
  }

  static getDerivedStateFromError(error) {
    return { error: error.message || String(error) };
  }

  componentDidCatch(error, info) {
    this.setState({ info: info.componentStack });
  }

  render() {
    if (this.state.error) {
      return (
        <div style={{
          padding: 40,
          color: '#f87171',
          fontFamily: "'Inter', sans-serif",
          maxWidth: 600,
          margin: '80px auto',
          background: '#141420',
          borderRadius: 12,
          border: '1px solid #28283E',
        }}>
          <h2 style={{ fontSize: 20, marginBottom: 12, display: 'flex', alignItems: 'center', gap: 10 }}>
            <i className="fas fa-exclamation-triangle"></i> Something went wrong
          </h2>
          <p style={{ color: '#A8A8C8', fontSize: 14, marginBottom: 16 }}>
            {this.state.error}
          </p>
          <button
            onClick={() => window.location.reload()}
            style={{
              padding: '10px 24px',
              borderRadius: 8,
              background: 'linear-gradient(135deg, #7C6CF8, #38D9FF)',
              border: 'none',
              color: '#fff',
              fontSize: 14,
              fontWeight: 600,
              cursor: 'pointer',
              fontFamily: "'Inter', sans-serif",
            }}
          >
            <i className="fas fa-redo"></i> Reload Page
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
