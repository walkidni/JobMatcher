import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // Disable credentials since we're using JWT
});

// Add a request interceptor to add the auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    console.log('Making request to:', config.url);
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.log('No token found in localStorage');
    }
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Add a response interceptor to handle token expiration
api.interceptors.response.use(
  (response) => {
    console.log('Response received:', response.status);
    return response;
  },
  async (error) => {
    console.error('Response error:', error.response?.status, error.response?.data);
    if (error.response?.status === 401) {
      console.log('Unauthorized, redirecting to login');
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const auth = {
  login: async (email: string, password: string) => {
    console.log('Attempting login for:', email);
    const response = await api.post('/auth/login', { email, password });
    console.log('Login response:', response.data);
    return response.data;
  },
  registerCandidate: async (email: string, password: string, name: string) => {
    const response = await api.post('/auth/register-candidate', { 
      email, 
      password, 
      fullName: name 
    });
    return response.data;
  },
  registerRecruiter: async (email: string, password: string, name: string) => {
    const response = await api.post('/auth/register-recruiter', { 
      email, 
      password, 
      fullName: name 
    });
    return response.data;
  },
  logout: async () => {
    console.log('Logging out, removing token');
    localStorage.removeItem('token');
  },
};

export const recruiters = {
  getProfile: async () => {
    const response = await api.get('/api/recruiter/me');
    return response.data;
  },
};

export const resumes = {
  upload: async (candidateId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post(`/api/candidate/${candidateId}/upload-resume`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  getText: async (candidateId: number) => {
    const response = await api.get(`/api/candidate/${candidateId}/resume-text`);
    return response.data;
  },
  getPdf: async (candidateId: number) => {
    const response = await api.get(`/api/candidate/${candidateId}/resume/file`, {
      responseType: 'blob',
      headers: { 'Accept': 'application/pdf' },
    });
    return response.data; // This will be a Blob
  },
};

export const jobPosts = {
  create: async (jobPost: {
    title: string;
    description: string;
    requiredSkills: string[];
  }) => {
    const response = await api.post('/api/job-posts', jobPost);
    return response.data;
  },
  getAll: async () => {
    const response = await api.get('/api/job-posts');
    return response.data;
  },
  getByRecruiter: async (recruiterId: number) => {
    const response = await api.get(`/api/job-posts/recruiter/${recruiterId}`);
    return response.data;
  },
};

export const applications = {
  applyToJob: async (candidateId: number, jobPostId: number) => {
    const response = await api.post(`/api/candidate/${candidateId}/apply/${jobPostId}`);
    return response.data;
  },
  getApplicationsForCandidate: async (candidateId: number) => {
    const response = await api.get(`/api/candidate/${candidateId}/applications`);
    return response.data;
  },
  getApplicationsForJob: async (jobPostId: number) => {
    const response = await api.get(`/api/job-posts/${jobPostId}/applications`);
    return response.data;
  },
  updateApplicationStatus: async (applicationId: number, status: string) => {
    const response = await api.patch(`/api/job-posts/applications/${applicationId}/status`, { status });
    return response.data;
  },
};

export default api; 