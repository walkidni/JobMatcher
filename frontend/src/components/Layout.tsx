import { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { auth } from '../lib/api';

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    try {
      await auth.logout();
      logout();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow">
        <nav className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 justify-between items-center">
            <div className="flex-shrink-0">
              <Link to="/" className="text-xl font-bold text-primary-600">
                JobMatcher
              </Link>
            </div>
            <div className="flex items-center space-x-4">
              {!user && (
                <>
                  <Link to="/login" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">Login</Link>
                  <Link to="/register" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">Register</Link>
                </>
              )}
              {user?.role === 'RECRUITER' && (
                <>
                  <Link to="/profil" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">My Profile</Link>
                  <Link to="/create-job" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">Post Job</Link>
                  <Link to="/jobs" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">My Jobs</Link>
                </>
              )}
              {user?.role === 'CANDIDATE' && (
                <>
                  <Link to="/my-jobs" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">Matched Jobs</Link>
                  <Link to="/upload-resume" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">Upload Resume</Link>
                  <Link to="/resumes" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">My Resume</Link>
                  <Link to="/my-applications" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">My Applications</Link>
                </>
              )}
              {user && (
                <button
                  onClick={handleLogout}
                  className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                >
                  Logout
                </button>
              )}
            </div>
          </div>
        </nav>
      </header>

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
} 