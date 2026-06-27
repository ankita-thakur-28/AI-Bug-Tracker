import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import RoleRoute from './routes/RoleRoute';
import Login from './pages/Login';
import Signup from './pages/Signup';

function DashboardRouter() {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;

  if (user.role === 'ADMIN') {
    return <div style={{ padding: 40, textAlign: 'center', color: 'var(--text-secondary)' }}>Admin Dashboard — coming in Phase 3</div>;
  }
  if (user.role === 'DEVELOPER') {
    return <div style={{ padding: 40, textAlign: 'center', color: 'var(--text-secondary)' }}>Developer Dashboard — coming in Phase 4</div>;
  }
  if (user.role === 'TESTER') {
    return <div style={{ padding: 40, textAlign: 'center', color: 'var(--text-secondary)' }}>Tester Dashboard — coming in Phase 5</div>;
  }

  return <Navigate to="/login" replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/" element={<ProtectedRoute><DashboardRouter /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
