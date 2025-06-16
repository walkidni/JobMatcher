import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import RecruiterDashboard from './pages/RecruiterDashboard';
import CreateJob from './pages/CreateJob';
import MyJobs from './pages/MyJobs';
import UploadResume from './pages/UploadResume';
import MyResume from './pages/MyResume';
import NotFound from './pages/NotFound';
import Layout from './components/Layout';

const queryClient = new QueryClient();

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      {/* Recruiter routes */}
      <Route
        path="/dashboard"
        element={
          <PrivateRoute role="RECRUITER">
            <Layout><RecruiterDashboard /></Layout>
          </PrivateRoute>
        }
      />
      <Route
        path="/create-job"
        element={
          <PrivateRoute role="RECRUITER">
            <Layout><CreateJob /></Layout>
          </PrivateRoute>
        }
      />
      <Route
        path="/jobs"
        element={
          <PrivateRoute role="RECRUITER">
            <Layout><MyJobs /></Layout>
          </PrivateRoute>
        }
      />
      {/* Candidate routes */}
      <Route
        path="/my-jobs"
        element={
          <PrivateRoute role="CANDIDATE">
            <Layout><MyJobs /></Layout>
          </PrivateRoute>
        }
      />
      <Route
        path="/upload-resume"
        element={
          <PrivateRoute role="CANDIDATE">
            <Layout><UploadResume /></Layout>
          </PrivateRoute>
        }
      />
      <Route
        path="/resumes"
        element={
          <PrivateRoute role="CANDIDATE">
            <Layout><MyResume /></Layout>
          </PrivateRoute>
        }
      />
      {/* Not found */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Router>
          <AppRoutes />
        </Router>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App; 