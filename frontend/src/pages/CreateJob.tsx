import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { jobPosts } from '../lib/api';

export default function CreateJob() {
  const { user } = useAuth();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [skillInput, setSkillInput] = useState('');
  const [requiredSkills, setRequiredSkills] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  if (user?.role !== 'RECRUITER') {
    return <div className="p-8 text-red-600">Only recruiters can post jobs.</div>;
  }

  const handleAddSkill = () => {
    const skill = skillInput.trim();
    if (skill && !requiredSkills.includes(skill)) {
      setRequiredSkills([...requiredSkills, skill]);
      setSkillInput('');
    }
  };

  const handleRemoveSkill = (skill: string) => {
    setRequiredSkills(requiredSkills.filter((s) => s !== skill));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await jobPosts.create({ title, description, requiredSkills });
      setSuccess('Job posted successfully!');
      setTitle('');
      setDescription('');
      setRequiredSkills([]);
      setSkillInput('');
    } catch (err) {
      setError('Failed to post job. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-8 max-w-xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Post a New Job</h1>
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700">Job Title</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            className="input mt-1 w-full"
            required
          />
        </div>
        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700">Job Description</label>
          <textarea
            id="description"
            value={description}
            onChange={e => setDescription(e.target.value)}
            className="input mt-1 w-full"
            rows={4}
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Required Skills</label>
          <div className="flex gap-2 mt-1">
            <input
              type="text"
              value={skillInput}
              onChange={e => setSkillInput(e.target.value)}
              className="input flex-1"
              placeholder="Add a skill"
            />
            <button type="button" onClick={handleAddSkill} className="btn btn-secondary">Add</button>
          </div>
          <div className="mt-2 flex flex-wrap gap-2">
            {requiredSkills.map(skill => (
              <span key={skill} className="inline-flex items-center rounded-full bg-primary-100 px-3 py-0.5 text-sm font-medium text-primary-800">
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
          {submitting ? 'Posting...' : 'Post Job'}
        </button>
        {success && <div className="text-green-600 text-sm mt-2">{success}</div>}
        {error && <div className="text-red-600 text-sm mt-2">{error}</div>}
      </form>
    </div>
  );
} 