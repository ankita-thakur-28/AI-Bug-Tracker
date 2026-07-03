import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ToastProvider } from './components/common/Toast';
import ErrorBoundary from './components/common/ErrorBoundary';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Forbidden from './pages/Forbidden';

function SimpleAdmin() {
  return (
    <div style={{padding:40,color:'white',fontFamily:'sans-serif'}}>
      <h1>Admin Dashboard</h1>
      <p>If you see this, routing and auth work correctly.</p>
    </div>
  );
}

function SimpleDev() {
  return <div style={{padding:40,color:'white'}}><h1>Developer Dashboard</h1></div>;
}

function SimpleTester() {
  return <div style={{padding:40,color:'white'}}><h1>Tester Dashboard</h1></div>;
}

function AppRoutes() {
  const { user } = useAuth();

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/" element={
        user.role === 'ADMIN' ? <SimpleAdmin /> :
        user.role === 'DEVELOPER' ? <SimpleDev /> :
        user.role === 'TESTER' ? <SimpleTester /> :
        <Navigate to="/login" replace />
      } />
      <Route path="/forbidden" element={<Forbidden />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <ToastProvider>
          <AppRoutes />
        </ToastProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
}
