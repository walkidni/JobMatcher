import { useState, useEffect } from 'react';
import { resumes } from '../lib/api';
import api from '../lib/api';

export default function CandidateDashboard() {
  const [profile, setProfile] = useState<{ id: number; name: string; email: string } | null>(null);
  const [resume, setResume] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [resumeText, setResumeText] = useState<string | null>(null);
  const [resumeLoading, setResumeLoading] = useState(false);
  const [resumeError, setResumeError] = useState('');
  const [profileLoading, setProfileLoading] = useState(true);
  const [profileError, setProfileError] = useState('');

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

  useEffect(() => {
    const fetchResumeText = async () => {
      if (!profile?.id) return;
      setResumeLoading(true);
      setResumeError('');
      try {
        const text = await resumes.getText(profile.id);
        setResumeText(text);
      } catch (err) {
        setResumeText(null);
        setResumeError('No resume found or failed to fetch resume.');
      } finally {
        setResumeLoading(false);
      }
    };
    if (profile?.id) fetchResumeText();
  }, [profile?.id]);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setResume(event.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!resume || !profile?.id) return;
    setUploading(true);
    try {
      await resumes.upload(profile.id, resume);
      alert('Resume uploaded successfully!');
      // Refetch resume text after upload
      const text = await resumes.getText(profile.id);
      setResumeText(text);
      setResumeError('');
    } catch (error) {
      alert('Failed to upload resume. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg font-medium leading-6 text-gray-900">
          Welcome back{profile?.name ? `, ${profile.name}` : ''}!
        </h3>
        <div className="mt-2 max-w-xl text-sm text-gray-500">
          <p>Manage your profile and job applications here.</p>
        </div>
        <div className="mt-5">
          <div className="rounded-md bg-gray-50 px-4 py-5 sm:p-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="rounded-lg bg-white p-4 shadow">
                <h4 className="text-lg font-medium text-gray-900">Profile</h4>
                {profileLoading ? (
                  <div className="text-sm text-gray-500">Loading profile...</div>
                ) : profileError ? (
                  <div className="text-sm text-red-500">{profileError}</div>
                ) : profile ? (
                  <dl className="mt-2 space-y-2">
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Name</dt>
                      <dd className="text-sm text-gray-900">{profile.name}</dd>
                    </div>
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Email</dt>
                      <dd className="text-sm text-gray-900">{profile.email}</dd>
                    </div>
                  </dl>
                ) : null}
              </div>
              <div className="rounded-lg bg-white p-4 shadow">
                <h4 className="text-lg font-medium text-gray-900">Resume</h4>
                <div className="mt-4 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Upload your resume
                    </label>
                    <div className="mt-1">
                      <input
                        type="file"
                        accept=".pdf,.doc,.docx"
                        onChange={handleFileChange}
                        className="block w-full text-sm text-gray-500
                          file:mr-4 file:py-2 file:px-4
                          file:rounded-md file:border-0
                          file:text-sm file:font-semibold
                          file:bg-primary-50 file:text-primary-700
                          hover:file:bg-primary-100"
                      />
                    </div>
                  </div>
                  <button
                    onClick={handleUpload}
                    disabled={!resume || uploading}
                    className="btn btn-primary w-full"
                  >
                    {uploading ? 'Uploading...' : 'Upload Resume'}
                  </button>
                  {resumeLoading ? (
                    <div className="text-sm text-gray-500">Loading resume...</div>
                  ) : resumeText ? (
                    <div className="mt-4">
                      <h5 className="font-semibold mb-2">Extracted Resume Text:</h5>
                      <pre className="whitespace-pre-wrap bg-gray-100 p-2 rounded text-xs max-h-60 overflow-y-auto">{resumeText}</pre>
                    </div>
                  ) : resumeError ? (
                    <div className="text-sm text-red-500">{resumeError}</div>
                  ) : null}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 