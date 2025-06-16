import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { resumes } from '../lib/api';
import api from '../lib/api';

export default function UploadResume() {
  const { user } = useAuth();
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setFile(event.target.files[0]);
      setSuccess('');
      setError('');
    }
  };

  const handleUpload = async () => {
    if (!file || !user || user.role !== 'CANDIDATE') {
      setError('You must be logged in as a candidate and select a file.');
      return;
    }
    setUploading(true);
    setError('');
    setSuccess('');
    try {
      // Fetch candidate id from /api/candidate/me
      const profileRes = await api.get('/api/candidate/me');
      const candidateId = profileRes.data.id;
      if (!candidateId) {
        setError('Could not determine candidate ID.');
        setUploading(false);
        return;
      }
      await resumes.upload(candidateId, file);
      setSuccess('Resume uploaded successfully!');
      setFile(null);
    } catch (err) {
      setError('Failed to upload resume. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Upload Resume</h1>
      {user?.role !== 'CANDIDATE' ? (
        <div className="text-red-600">Only candidates can upload resumes.</div>
      ) : (
        <div className="bg-white rounded-lg shadow p-4 max-w-md">
          <input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-500 mb-4"
          />
          <button
            onClick={handleUpload}
            disabled={!file || uploading}
            className="btn btn-primary w-full mb-2"
          >
            {uploading ? 'Uploading...' : 'Upload Resume'}
          </button>
          {success && <div className="text-green-600 text-sm mb-2">{success}</div>}
          {error && <div className="text-red-600 text-sm mb-2">{error}</div>}
        </div>
      )}
    </div>
  );
} 