import React, { createContext, useContext, useMemo, useState } from 'react';
import api from '../api/client';

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  roles: string[];
  userId: string | null;
  fullName: string | null;
};

type AuthContextValue = AuthState & {
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, fullName: string) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const initialState: AuthState = {
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  roles: JSON.parse(localStorage.getItem('roles') || '[]'),
  userId: localStorage.getItem('userId'),
  fullName: localStorage.getItem('fullName'),
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(initialState);

  React.useEffect(() => {
    const fetchProfile = async () => {
      if (state.accessToken && !state.userId) {
        const profile = await api.get('/api/auth/me');
        localStorage.setItem('userId', profile.data.id);
        localStorage.setItem('fullName', profile.data.fullName);
        setState((prev) => ({ ...prev, userId: profile.data.id, fullName: profile.data.fullName }));
      }
    };
    fetchProfile();
  }, [state.accessToken, state.userId]);

  const login = async (email: string, password: string) => {
    const response = await api.post('/api/auth/login', { email, password });
    const { accessToken, refreshToken, roles } = response.data;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('roles', JSON.stringify(roles));
    const profile = await api.get('/api/auth/me');
    localStorage.setItem('userId', profile.data.id);
    localStorage.setItem('fullName', profile.data.fullName);
    setState({ accessToken, refreshToken, roles: Array.from(roles), userId: profile.data.id, fullName: profile.data.fullName });
  };

  const register = async (email: string, password: string, fullName: string) => {
    const response = await api.post('/api/auth/register', { email, password, fullName });
    const { accessToken, refreshToken, roles } = response.data;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('roles', JSON.stringify(roles));
    const profile = await api.get('/api/auth/me');
    localStorage.setItem('userId', profile.data.id);
    localStorage.setItem('fullName', profile.data.fullName);
    setState({ accessToken, refreshToken, roles: Array.from(roles), userId: profile.data.id, fullName: profile.data.fullName });
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      await api.post('/api/auth/logout', { refreshToken });
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('roles');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
    setState({ accessToken: null, refreshToken: null, roles: [], userId: null, fullName: null });
  };

  const hasRole = (role: string) => state.roles.includes(role);

  const value = useMemo(
    () => ({ ...state, login, register, logout, hasRole }),
    [state]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};
