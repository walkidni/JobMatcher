export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
      <h1 className="text-4xl font-bold mb-4">Welcome to Job Matcher</h1>
      <p className="mb-8 text-lg text-gray-700">Find your dream job or the perfect candidate, powered by smart matching.</p>
      <div className="flex gap-4">
        <a href="/register" className="btn btn-primary">Get Started</a>
        <a href="/login" className="btn btn-secondary">Login</a>
      </div>
    </div>
  );
} 