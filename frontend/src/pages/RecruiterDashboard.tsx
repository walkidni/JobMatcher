import { useState, useEffect } from 'react';
import { recruiters, jobPosts } from '../lib/api';

interface JobPost {
  id: number;
  title: string;
  description: string;
  requiredSkills: string[];
}

interface RecruiterProfile {
  id: number;
  fullName: string;
  email: string;
  company: string;
}

export default function RecruiterDashboard() {
  const [profile, setProfile] = useState<RecruiterProfile | null>(null);
  const [myJobPosts, setMyJobPosts] = useState<JobPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [jobPost, setJobPost] = useState<Omit<JobPost, 'id'>>({
    title: '',
    description: '',
    requiredSkills: [],
  });
  const [skillInput, setSkillInput] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const profileData = await recruiters.getProfile();
        setProfile(profileData);
        
        // Only fetch job posts if we have a valid profile with ID
        if (profileData?.id) {
          const jobPostsData = await jobPosts.getByRecruiter(profileData.id);
          setMyJobPosts(jobPostsData);
        }
        
        setError(null);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleAddSkill = () => {
    if (skillInput.trim() && !jobPost.requiredSkills.includes(skillInput.trim())) {
      setJobPost({
        ...jobPost,
        requiredSkills: [...jobPost.requiredSkills, skillInput.trim()],
      });
      setSkillInput('');
    }
  };

  const handleRemoveSkill = (skill: string) => {
    setJobPost({
      ...jobPost,
      requiredSkills: jobPost.requiredSkills.filter((s) => s !== skill),
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!profile?.id) {
      alert('Profile not loaded. Please try again.');
      return;
    }
    
    setSubmitting(true);
    try {
      const newJobPost = await jobPosts.create(jobPost);
      setMyJobPosts([...myJobPosts, newJobPost]);
      setJobPost({ title: '', description: '', requiredSkills: [] });
      alert('Job post created successfully!');
    } catch (error) {
      alert('Failed to create job post. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Recruiter Dashboard</h1>
        {profile && (
          <div className="mt-4 rounded-lg bg-white p-6 shadow">
            <h2 className="text-xl font-semibold text-gray-900">Profile Information</h2>
            <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <p className="text-sm font-medium text-gray-500">Full Name</p>
                <p className="mt-1 text-lg text-gray-900">{profile.fullName}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">Email</p>
                <p className="mt-1 text-lg text-gray-900">{profile.email}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">Company</p>
                <p className="mt-1 text-lg text-gray-900">{profile.company}</p>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
        <div>
          <div className="rounded-lg bg-white p-4 shadow">
            <h4 className="text-lg font-medium text-gray-900">My Job Posts</h4>
            <div className="mt-4 space-y-4">
              {myJobPosts.length === 0 ? (
                <p className="text-gray-500">No job posts yet. Create your first job post!</p>
              ) : (
                myJobPosts.map((post) => (
                  <div key={post.id} className="rounded-lg border p-4">
                    <h5 className="text-lg font-medium text-gray-900">{post.title}</h5>
                    <p className="mt-2 text-gray-600">{post.description}</p>
                    <div className="mt-2 flex flex-wrap gap-2">
                      {post.requiredSkills.map((skill) => (
                        <span
                          key={skill}
                          className="inline-flex items-center rounded-full bg-primary-100 px-3 py-0.5 text-sm font-medium text-primary-800"
                        >
                          {skill}
                        </span>
                      ))}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        <div>
          <div className="rounded-lg bg-white p-4 shadow">
            <h4 className="text-lg font-medium text-gray-900">Create Job Post</h4>
            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                  Job Title
                </label>
                <input
                  type="text"
                  id="title"
                  value={jobPost.title}
                  onChange={(e) => setJobPost({ ...jobPost, title: e.target.value })}
                  className="input mt-1"
                  required
                />
              </div>
              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                  Job Description
                </label>
                <textarea
                  id="description"
                  value={jobPost.description}
                  onChange={(e) => setJobPost({ ...jobPost, description: e.target.value })}
                  rows={4}
                  className="input mt-1"
                  required
                />
              </div>
              <div>
                <label htmlFor="skills" className="block text-sm font-medium text-gray-700">
                  Required Skills
                </label>
                <div className="mt-1 flex space-x-2">
                  <input
                    type="text"
                    id="skills"
                    value={skillInput}
                    onChange={(e) => setSkillInput(e.target.value)}
                    className="input flex-1"
                    placeholder="Add a skill"
                  />
                  <button
                    type="button"
                    onClick={handleAddSkill}
                    className="btn btn-secondary"
                  >
                    Add
                  </button>
                </div>
                <div className="mt-2 flex flex-wrap gap-2">
                  {jobPost.requiredSkills.map((skill) => (
                    <span
                      key={skill}
                      className="inline-flex items-center rounded-full bg-primary-100 px-3 py-0.5 text-sm font-medium text-primary-800"
                    >
                      {skill}
                      <button
                        type="button"
                        onClick={() => handleRemoveSkill(skill)}
                        className="ml-1 inline-flex h-4 w-4 flex-shrink-0 items-center justify-center rounded-full text-primary-400 hover:bg-primary-200 hover:text-primary-500"
                      >
                        ×
                      </button>
                    </span>
                  ))}
                </div>
              </div>
              <button
                type="submit"
                disabled={submitting}
                className="btn btn-primary w-full"
              >
                {submitting ? 'Creating...' : 'Create Job Post'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
} 