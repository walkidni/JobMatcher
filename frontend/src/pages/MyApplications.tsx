import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { applications } from '../lib/api';
import api from '../lib/api';

interface Application {
  applicationId: number;
  jobPostId: number;
  jobTitle: string;
  jobDescription: string;
  status: string;
  appliedAt: string;
}

export default function MyApplications() {
  const { user } = useAuth();
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchApplications = async () => {
      setLoading(true);
      setError('');
      try {
        if (user?.role === 'CANDIDATE') {
          const profileRes = await api.get('/api/candidate/me');
          const profile = profileRes.data;
          const appsRes = await applications.getApplicationsForCandidate(profile.id);
          setApps(appsRes);
        }
      } catch (err) {
        setError('Failed to fetch applications.');
      } finally {
        setLoading(false);
      }
    };
    if (user) fetchApplications();
  }, [user]);

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">My Applications</h1>
      {loading ? (
        <div>Loading...</div>
      ) : error ? (
        <div className="text-red-600">{error}</div>
      ) : apps.length === 0 ? (
        <div>No applications found.</div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white rounded shadow">
            <thead>
              <tr>
                <th className="px-4 py-2 text-left">Job Title</th>
                <th className="px-4 py-2 text-left">Status</th>
                <th className="px-4 py-2 text-left">Applied At</th>
              </tr>
            </thead>
            <tbody>
              {apps.map(app => (
                <tr key={app.applicationId} className="border-b">
                  <td className="px-4 py-2 font-medium">{app.jobTitle}</td>
                  <td className="px-4 py-2">{app.status}</td>
                  <td className="px-4 py-2">{new Date(app.appliedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
} 