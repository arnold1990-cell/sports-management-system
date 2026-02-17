import React, { createContext, useContext, useMemo, useState } from 'react';
import api from '../api/client';

type AuthUser = {
  id: string;
  email: string;
  fullName: string;
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
const AUTH_LOGIN_ENDPOINT = '/api/auth/login';

const sanitizeToken = (token: string | null): string | null => {
  if (!token) {
    return null;
  }
  const normalized = token.trim().toLowerCase();
  if (!normalized || normalized === 'null' || normalized === 'undefined') {
    return null;
  }
  return token;
};

const loadStoredRoles = () => {
  try {
    const stored = JSON.parse(localStorage.getItem('roles') || '[]');
    return Array.isArray(stored) ? stored : [];
  } catch {
    return [];
  }
};

const initialAccessToken = sanitizeToken(localStorage.getItem('accessToken'));
const initialRefreshToken = sanitizeToken(localStorage.getItem('refreshToken'));

const initialState: AuthState = {
  accessToken: initialAccessToken,
  refreshToken: initialRefreshToken,
  roles: loadStoredRoles(),
  user: localStorage.getItem('userId')
    ? {
        id: localStorage.getItem('userId') as string,
        email: localStorage.getItem('email') || '',
        fullName: localStorage.getItem('fullName') || ''
      }
    : null
};

const persistAuth = (accessToken: string, refreshToken: string, roles: string[], user: AuthUser | null) => {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  localStorage.setItem('roles', JSON.stringify(roles));

  if (user) {
    localStorage.setItem('userId', user.id);
    localStorage.setItem('email', user.email);
    localStorage.setItem('fullName', user.fullName);
  }
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
    return { id: profile.data.id, email: profile.data.email, fullName: profile.data.fullName };
  }, []);

  React.useEffect(() => {
    const syncStoredSession = async () => {
      const token = sanitizeToken(state.accessToken);
      if (!token) {
        if (state.accessToken) {
          clearAuth();
        }
        return;
      }

      if (state.user) {
        return;
      }

      try {
        const user = await fetchProfile();
        persistAuth(token, state.refreshToken || '', state.roles, user);
        setState((prev) => ({ ...prev, user }));
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
    const response = await api.post(AUTH_LOGIN_ENDPOINT, { email, password });
    const { accessToken, refreshToken, roles } = response.data;

    persistAuth(accessToken, refreshToken, roles, null);
    setState({ accessToken, refreshToken, roles, user: null });

    const user = await fetchProfile();
    persistAuth(accessToken, refreshToken, roles, user);
    setState({ accessToken, refreshToken, roles, user });
  };

  const register = async (email: string, password: string, fullName: string) => {
    const response = await api.post('/api/auth/register', { email, password, fullName });
    const { accessToken, refreshToken, roles } = response.data;

    persistAuth(accessToken, refreshToken, roles, null);
    setState({ accessToken, refreshToken, roles, user: null });

    const user = await fetchProfile();
    persistAuth(accessToken, refreshToken, roles, user);
    setState({ accessToken, refreshToken, roles, user });
  };

  const logout = async () => {
    const refreshToken = sanitizeToken(localStorage.getItem('refreshToken'));
    if (refreshToken) {
      await api.post('/api/auth/logout', { refreshToken });
    }
    clearAuth();
  };

  const hasRole = (role: string) => state.roles.includes(role);

  const value = useMemo(
    () => ({
      ...state,
      isAuthenticated: Boolean(sanitizeToken(state.accessToken)),
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
