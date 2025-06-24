import { useEffect, useState } from 'react';
import api, { resumes } from '../lib/api';
import { Document, Page, pdfjs } from 'react-pdf';
// @ts-ignore
import pdfWorker from 'pdfjs-dist/build/pdf.worker?url';
import 'react-pdf/dist/esm/Page/TextLayer.css';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';

pdfjs.GlobalWorkerOptions.workerSrc = pdfWorker;

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
  const [showPdf, setShowPdf] = useState(false);
  const [numPages, setNumPages] = useState<number | null>(null);
  const [pdfError, setPdfError] = useState('');
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [downloadLoading, setDownloadLoading] = useState(false);

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
    } catch (err: any) {
      setResumeText(null);
      setFileName(null);
      if (err.response && err.response.status === 404) {
        setResumeError('No resume available.');
      } else {
        setResumeError('Failed to fetch resume.');
      }
    } finally {
      setResumeLoading(false);
    }
  };

  useEffect(() => {
    if (profile?.id) fetchResume(profile.id);
    if (profile?.id) setPdfUrl(`/api/candidate/${profile.id}/resume/file`);
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

  const handlePreviewPdf = async () => {
    if (!profile?.id) return;
    setPdfError('');
    setPdfLoading(true);
    try {
      const blob = await resumes.getPdf(profile.id);
      const url = URL.createObjectURL(blob);
      setPdfUrl(url);
      setShowPdf(true);
    } catch (err) {
      setPdfError('Failed to load PDF.');
    } finally {
      setPdfLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!profile?.id) return;
    setDownloadLoading(true);
    try {
      const blob = await resumes.getPdf(profile.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName || 'resume.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to download PDF.');
    } finally {
      setDownloadLoading(false);
    }
  };

  useEffect(() => {
    return () => {
      if (pdfUrl) URL.revokeObjectURL(pdfUrl);
    };
  }, [pdfUrl]);

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
        <div className={resumeError === 'No resume available.' ? 'text-gray-500' : 'text-red-500'}>{resumeError}</div>
      ) : resumeText ? (
        <div className="bg-white rounded-lg shadow p-4">
          <div className="mb-2 font-semibold">File Name: {fileName || 'Unknown'}</div>
          <button
            onClick={handleDelete}
            disabled={deleting}
            className="btn btn-danger mt-4"
          >
            {deleting ? 'Deleting...' : 'Delete Resume'}
          </button>
          <button
            onClick={handleDownloadPdf}
            disabled={downloadLoading}
            className="btn btn-secondary mt-4 ml-4"
          >
            {downloadLoading ? 'Downloading...' : 'Download Resume'}
          </button>
          {deleteSuccess && <div className="text-green-600 text-sm mt-2">{deleteSuccess}</div>}
          <div className="mt-6">
            <button
              className="btn btn-primary"
              onClick={showPdf ? () => { setShowPdf(false); setPdfUrl(null); } : handlePreviewPdf}
              disabled={pdfLoading}
            >
              {showPdf ? 'Hide PDF Preview' : pdfLoading ? 'Loading PDF...' : 'Preview PDF'}
            </button>
            {showPdf && pdfUrl && (
              <div className="mt-4 border rounded shadow p-2 bg-gray-50">
                <Document
                  file={pdfUrl}
                  onLoadSuccess={({ numPages }) => setNumPages(numPages)}
                  onLoadError={err => setPdfError('Failed to load PDF.')}
                >
                  {Array.from(new Array(numPages), (el, index) => (
                    <Page key={`page_${index + 1}`} pageNumber={index + 1} />
                  ))}
                </Document>
                {pdfError && <div className="text-red-500">{pdfError}</div>}
              </div>
            )}
          </div>
        </div>
      ) : (
        <div>No resume uploaded yet.</div>
      )}
    </div>
  );
} 