import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api, { jobPosts, resumes, applications } from '../lib/api';

interface JobPost {
  id: number;
  title: string;
  description: string;
  requiredSkills: string[];
  matchScore?: number;
}

interface CandidateMatch {
  id: number;
  email: string;
  name?: string;
  resumeText?: string;
  matchScore: number;
}

export default function MyJobs() {
  const { user } = useAuth();
  const [jobs, setJobs] = useState<JobPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalJobId, setModalJobId] = useState<number | null>(null);
  const [candidates, setCandidates] = useState<CandidateMatch[]>([]);
  const [candidatesLoading, setCandidatesLoading] = useState(false);
  const [candidatesError, setCandidatesError] = useState('');
  const [downloadLoadingId, setDownloadLoadingId] = useState<number | null>(null);
  const [appliedJobIds, setAppliedJobIds] = useState<number[]>([]);
  const [applyingJobId, setApplyingJobId] = useState<number | null>(null);
  const [applicationsModalJobId, setApplicationsModalJobId] = useState<number | null>(null);
  const [applicationsList, setApplicationsList] = useState<any[]>([]);
  const [applicationsLoading, setApplicationsLoading] = useState(false);
  const [statusUpdatingId, setStatusUpdatingId] = useState<number | null>(null);
  const [downloadAppLoadingId, setDownloadAppLoadingId] = useState<number | null>(null);

  useEffect(() => {
    const fetchJobs = async () => {
      setLoading(true);
      setError('');
      try {
        if (user?.role === 'RECRUITER') {
          // Fetch jobs posted by this recruiter
          const profileRes = await api.get('/api/recruiter/me');
          const recruiterId = profileRes.data.id;
          const jobsRes = await jobPosts.getByRecruiter(recruiterId);
          setJobs(jobsRes);
        } else if (user?.role === 'CANDIDATE') {
          // Fetch matched jobs for this candidate
          const profileRes = await api.get('/api/candidate/me');
          const candidateId = profileRes.data.id;
          const matchesRes = await api.get(`/api/match/candidate/${candidateId}/jobs`);
          setJobs(matchesRes.data);
          // Fetch applied jobs for candidate
          const applicationsRes = await applications.getApplicationsForCandidate(profileRes.data.id);
          setAppliedJobIds(applicationsRes.map((app: any) => app.jobPostId));
        }
      } catch (err) {
        setError('Failed to fetch jobs.');
      } finally {
        setLoading(false);
      }
    };
    if (user) fetchJobs();
  }, [user]);

  const handleViewCandidates = async (jobId: number) => {
    setModalJobId(jobId);
    setCandidatesLoading(true);
    setCandidatesError('');
    try {
      const res = await api.get(`/api/match/job/${jobId}/candidates`);
      setCandidates(res.data);
    } catch (err) {
      setCandidatesError('Failed to fetch matched candidates.');
      setCandidates([]);
    } finally {
      setCandidatesLoading(false);
    }
  };

  const handleCloseModal = () => {
    setModalJobId(null);
    setCandidates([]);
    setCandidatesError('');
  };

  const handleDownloadResume = async (candidateId: number, candidateName?: string) => {
    setDownloadLoadingId(candidateId);
    try {
      const blob = await resumes.getPdf(candidateId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = candidateName ? `${candidateName}-resume.pdf` : 'resume.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to download resume.');
    } finally {
      setDownloadLoadingId(null);
    }
  };

  const handleApply = async (jobId: number) => {
    if (!user) return;
    setApplyingJobId(jobId);
    try {
      const profileRes = await api.get('/api/candidate/me');
      const candidateId = profileRes.data.id;
      await applications.applyToJob(candidateId, jobId);
      setAppliedJobIds((prev) => [...prev, jobId]);
      alert('Application submitted successfully!');
    } catch (err: any) {
      alert(err?.response?.data || 'Failed to apply.');
    } finally {
      setApplyingJobId(null);
    }
  };

  const handleViewApplications = async (jobId: number) => {
    setApplicationsModalJobId(jobId);
    setApplicationsLoading(true);
    try {
      const apps = await applications.getApplicationsForJob(jobId);
      setApplicationsList(apps);
    } catch (err) {
      setApplicationsList([]);
    } finally {
      setApplicationsLoading(false);
    }
  };

  const handleCloseApplications = () => {
    setApplicationsModalJobId(null);
    setApplicationsList([]);
  };

  const handleStatusUpdate = async (applicationId: number, status: string) => {
    setStatusUpdatingId(applicationId);
    try {
      await applications.updateApplicationStatus(applicationId, status);
      setApplicationsList((prev) => prev.map(app => app.applicationId === applicationId ? { ...app, status } : app));
      alert('Status updated!');
    } catch (err) {
      alert('Failed to update status.');
    } finally {
      setStatusUpdatingId(null);
    }
  };

  const handleDownloadResumeApp = async (candidateId: number, candidateName?: string) => {
    setDownloadAppLoadingId(candidateId);
    try {
      const blob = await resumes.getPdf(candidateId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = candidateName ? `${candidateName}-resume.pdf` : 'resume.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to download resume.');
    } finally {
      setDownloadAppLoadingId(null);
    }
  };

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }
  if (error && jobs.length > 0) {
    return <div className="p-8 text-red-600">{error}</div>;
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">
        {user?.role === 'RECRUITER' ? 'My Posted Jobs' : 'Matched Jobs'}
      </h1>
      {jobs.length === 0 ? (
        <p>{user?.role === 'RECRUITER' ? 'No jobs posted yet.' : 'No matched jobs available.'}</p>
      ) : (
        <div className="space-y-4">
          {jobs.map((job) => (
            <div key={job.id} className="rounded-lg border p-4 bg-white shadow">
              <h2 className="text-lg font-semibold">{job.title}</h2>
              <p className="text-gray-700 mb-2">{job.description}</p>
              <div className="flex flex-wrap gap-2 mb-2">
                {job.requiredSkills.map((skill) => (
                  <span key={skill} className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full text-xs">{skill}</span>
                ))}
              </div>
              {user?.role === 'CANDIDATE' && job.matchScore !== undefined && (
                <div className="text-sm text-green-700 font-medium">Match Score: {job.matchScore}</div>
              )}
              {user?.role === 'CANDIDATE' && (
                <button
                  className="btn btn-primary mt-2"
                  onClick={() => handleApply(job.id)}
                  disabled={appliedJobIds.includes(job.id) || applyingJobId === job.id}
                >
                  {appliedJobIds.includes(job.id)
                    ? 'Applied'
                    : applyingJobId === job.id
                    ? 'Applying...'
                    : 'Apply'}
                </button>
              )}
              {user?.role === 'RECRUITER' && (
                <button
                  className="btn btn-secondary mt-2"
                  onClick={() => handleViewCandidates(job.id)}
                >
                  View Matched Candidates
                </button>
              )}
              {user?.role === 'RECRUITER' && (
                <button
                  className="btn btn-secondary mt-2"
                  onClick={() => handleViewApplications(job.id)}
                >
                  View Applications
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Modal for matched candidates */}
      {modalJobId !== null && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-lg relative">
            <button
              onClick={handleCloseModal}
              className="absolute top-2 right-2 text-gray-500 hover:text-gray-800 text-2xl font-bold"
              aria-label="Close"
            >
              ×
            </button>
            <h2 className="text-xl font-bold mb-4">Matched Candidates</h2>
            {candidatesLoading ? (
              <div>Loading matched candidates...</div>
            ) : candidatesError ? (
              <div className="text-red-600">{candidatesError}</div>
            ) : candidates.length > 0 ? (
              <ul className="list-disc pl-5">
                {candidates.map((cand) => (
                  <li key={cand.id} className="mb-4">
                    <div>
                      <span className="font-medium">{cand.name || 'Unknown Name'}</span> (<span>{cand.email}</span>)
                      <span className="ml-2 text-green-700">Score: {cand.matchScore}</span>
                    </div>
                    <div className="mt-1">
                      <button
                        className="btn btn-primary text-xs"
                        onClick={() => handleDownloadResume(cand.id, cand.name)}
                        disabled={downloadLoadingId === cand.id}
                      >
                        {downloadLoadingId === cand.id ? 'Downloading...' : 'Download Resume'}
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <div>No matched candidates found.</div>
            )}
          </div>
        </div>
      )}

      {applicationsModalJobId !== null && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-lg relative">
            <button
              onClick={handleCloseApplications}
              className="absolute top-2 right-2 text-gray-500 hover:text-gray-800 text-2xl font-bold"
              aria-label="Close"
            >
              ×
            </button>
            <h2 className="text-xl font-bold mb-4">Applications</h2>
            {applicationsLoading ? (
              <div>Loading applications...</div>
            ) : applicationsList.length > 0 ? (
              <ul className="list-disc pl-5">
                {applicationsList.map((app) => (
                  <li key={app.candidateId} className="mb-4">
                    <div>
                      <span className="font-medium">{app.candidateName}</span> (<span>{app.candidateEmail}</span>)
                      <span className="ml-2">Status: {app.status}</span>
                    </div>
                    <div className="mt-1 flex gap-2 flex-wrap">
                      {["APPLIED","REVIEWED","REJECTED","ACCEPTED"].map(status => (
                        <button
                          key={status}
                          className={`btn btn-xs ${app.status === status ? 'btn-primary' : 'btn-secondary'}`}
                          onClick={() => handleStatusUpdate(app.applicationId, status)}
                          disabled={statusUpdatingId === app.applicationId || app.status === status}
                        >
                          {status}
                        </button>
                      ))}
                    </div>
                    <div className="mt-2">
                      <button
                        className="btn btn-primary btn-xs"
                        onClick={() => handleDownloadResumeApp(app.candidateId, app.candidateName)}
                        disabled={downloadAppLoadingId === app.candidateId}
                      >
                        {downloadAppLoadingId === app.candidateId ? 'Downloading...' : 'Download Resume'}
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <div>No applications found.</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
} 