import React, { createContext, useContext, useMemo, useState } from 'react';
import api from '../api/client';

type AuthUser = {
  id: string;
  email: string;
  fullName: string;
  roles: string[];
};

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  roles: string[];
  user: AuthUser | null;
};

type AuthContextValue = AuthState & {
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, fullName: string) => Promise<void>;
  logout: () => Promise<void>;
  clearAuth: () => void;
  hasRole: (role: string) => boolean;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const initialState: AuthState = {
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  roles: [],
  user: null
};

const persistAuth = (accessToken: string, refreshToken: string, roles: string[], user: AuthUser) => {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  localStorage.setItem('roles', JSON.stringify(roles));
  localStorage.setItem('userId', user.id);
  localStorage.setItem('email', user.email);
  localStorage.setItem('fullName', user.fullName);
};

const clearStoredAuth = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('roles');
  localStorage.removeItem('userId');
  localStorage.removeItem('email');
  localStorage.removeItem('fullName');
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(initialState);

  const clearAuth = React.useCallback(() => {
    clearStoredAuth();
    setState({ accessToken: null, refreshToken: null, roles: [], user: null });
  }, []);

  const fetchProfile = React.useCallback(async (): Promise<AuthUser> => {
    const profile = await api.get('/api/auth/me');
    const profileRoles = Array.isArray(profile.data.roles) ? profile.data.roles : [];
    return {
      id: profile.data.id,
      email: profile.data.email,
      fullName: profile.data.fullName,
      roles: profileRoles
    };
  }, []);

  React.useEffect(() => {
    const syncStoredSession = async () => {
      if (!state.accessToken || state.user) {
        return;
      }
      try {
        const user = await fetchProfile();
        persistAuth(state.accessToken, state.refreshToken || '', user.roles, user);
        setState((prev) => ({ ...prev, roles: user.roles, user }));
      } catch {
        clearAuth();
      }
    };
    syncStoredSession();
  }, [clearAuth, fetchProfile, state.accessToken, state.refreshToken, state.roles, state.user]);

  React.useEffect(() => {
    (window as any).__clearSportsMsAuth = clearAuth;
    return () => {
      delete (window as any).__clearSportsMsAuth;
    };
  }, [clearAuth]);

  const login = async (email: string, password: string) => {
    const response = await api.post('/api/auth/login', { email, password });
    const { accessToken, refreshToken } = response.data;
    const user = await fetchProfile();
    persistAuth(accessToken, refreshToken, user.roles, user);
    setState({ accessToken, refreshToken, roles: user.roles, user });
  };

  const register = async (email: string, password: string, fullName: string) => {
    const response = await api.post('/api/auth/register', { email, password, fullName });
    const { accessToken, refreshToken } = response.data;
    const user = await fetchProfile();
    persistAuth(accessToken, refreshToken, user.roles, user);
    setState({ accessToken, refreshToken, roles: user.roles, user });
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      await api.post('/api/auth/logout', { refreshToken });
    }
    clearAuth();
  };

  const hasRole = (role: string) => state.roles.includes(role);

  const value = useMemo(
    () => ({
      ...state,
      isAuthenticated: Boolean(state.accessToken && state.user),
      login,
      register,
      logout,
      clearAuth,
      hasRole
    }),
    [state, clearAuth]
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
