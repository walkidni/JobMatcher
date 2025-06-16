import { useAuthStore } from '../stores/auth';

export default function Dashboard() {
  const user = useAuthStore((state) => state.user);

  return (
    <div className="bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg font-medium leading-6 text-gray-900">
          Welcome back, {user?.name}!
        </h3>
        <div className="mt-2 max-w-xl text-sm text-gray-500">
          <p>This is your dashboard. More features coming soon!</p>
        </div>
        <div className="mt-5">
          <div className="rounded-md bg-gray-50 px-4 py-5 sm:p-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="rounded-lg bg-white p-4 shadow">
                <h4 className="text-lg font-medium text-gray-900">Profile</h4>
                <dl className="mt-2 space-y-2">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Name</dt>
                    <dd className="text-sm text-gray-900">{user?.name}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Email</dt>
                    <dd className="text-sm text-gray-900">{user?.email}</dd>
                  </div>
                </dl>
              </div>
              <div className="rounded-lg bg-white p-4 shadow">
                <h4 className="text-lg font-medium text-gray-900">Quick Actions</h4>
                <div className="mt-4 space-y-2">
                  <button className="btn btn-primary w-full">
                    Update Profile
                  </button>
                  <button className="btn btn-secondary w-full">
                    View Settings
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 