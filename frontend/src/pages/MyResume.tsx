import { useEffect, useState } from 'react';
import api, { resumes } from '../lib/api';

export default function MyResume() {
  const [profile, setProfile] = useState<{ id: number; name: string; email: string } | null>(null);
  const [resumeText, setResumeText] = useState<string | null>(null);
  const [resumeLoading, setResumeLoading] = useState(false);
  const [resumeError, setResumeError] = useState('');
  const [fileName, setFileName] = useState<string | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [profileError, setProfileError] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      setProfileLoading(true);
      setProfileError('');
      try {
        const response = await api.get('/api/candidate/me');
        setProfile(response.data);
      } catch (err) {
        setProfileError('Failed to fetch profile.');
      } finally {
        setProfileLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const fetchResume = async (candidateId: number) => {
    setResumeLoading(true);
    setResumeError('');
    try {
      const text = await resumes.getText(candidateId);
      setResumeText(text);
      const resumeRes = await api.get(`/api/candidate/${candidateId}/resume`);
      setFileName(resumeRes.data.originalFileName);
    } catch (err) {
      setResumeText(null);
      setFileName(null);
      setResumeError('No resume found or failed to fetch resume.');
    } finally {
      setResumeLoading(false);
    }
  };

  useEffect(() => {
    if (profile?.id) fetchResume(profile.id);
  }, [profile?.id]);

  const handleDelete = async () => {
    if (!profile?.id) return;
    setDeleting(true);
    setDeleteSuccess('');
    setResumeError('');
    try {
      await api.delete(`/api/candidate/${profile.id}/resume`);
      setResumeText(null);
      setFileName(null);
      setDeleteSuccess('Resume deleted successfully.');
    } catch (err) {
      setResumeError('Failed to delete resume.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">My Resume</h1>
      {profileLoading ? (
        <div className="text-gray-500">Loading profile...</div>
      ) : profileError ? (
        <div className="text-red-500">{profileError}</div>
      ) : resumeLoading ? (
        <div className="text-gray-500">Loading resume...</div>
      ) : resumeError ? (
        <div className="text-red-500">{resumeError}</div>
      ) : resumeText ? (
        <div className="bg-white rounded-lg shadow p-4">
          <div className="mb-2 font-semibold">File Name: {fileName || 'Unknown'}</div>
          <h5 className="font-semibold mb-2">Extracted Resume Text:</h5>
          <pre className="whitespace-pre-wrap bg-gray-100 p-2 rounded text-xs max-h-60 overflow-y-auto">{resumeText}</pre>
          <button
            onClick={handleDelete}
            disabled={deleting}
            className="btn btn-danger mt-4"
          >
            {deleting ? 'Deleting...' : 'Delete Resume'}
          </button>
          {deleteSuccess && <div className="text-green-600 text-sm mt-2">{deleteSuccess}</div>}
        </div>
      ) : (
        <div>No resume uploaded yet.</div>
      )}
    </div>
  );
} 