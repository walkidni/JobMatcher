import { create } from 'zustand';
import { jwtDecode } from 'jwt-decode';

interface User {
  id: string;
  email: string;
  name: string;
  role: 'CANDIDATE' | 'RECRUITER';
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setAuth: (token: string) => void;
  logout: () => void;
  initialize: () => void;
}

const initializeFromToken = (token: string | null): { user: User | null; isAuthenticated: boolean } => {
  if (!token) {
    return { user: null, isAuthenticated: false };
  }

  try {
    const decoded = jwtDecode<{ sub: string; email: string; name: string; role: 'CANDIDATE' | 'RECRUITER' }>(token);
    return {
      user: {
        id: decoded.sub,
        email: decoded.email,
        name: decoded.name,
        role: decoded.role,
      },
      isAuthenticated: true,
    };
  } catch (error) {
    console.error('Error decoding token:', error);
    localStorage.removeItem('token');
    return { user: null, isAuthenticated: false };
  }
};

export const useAuthStore = create<AuthState>((set) => {
  const token = localStorage.getItem('token');
  const { user, isAuthenticated } = initializeFromToken(token);

  return {
    user,
    isAuthenticated,
    setAuth: (token: string) => {
      localStorage.setItem('token', token);
      const { user, isAuthenticated } = initializeFromToken(token);
      set({ user, isAuthenticated });
    },
    logout: () => {
      localStorage.removeItem('token');
      set({ user: null, isAuthenticated: false });
    },
    initialize: () => {
      const token = localStorage.getItem('token');
      const { user, isAuthenticated } = initializeFromToken(token);
      set({ user, isAuthenticated });
    },
  };
});
