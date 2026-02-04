import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface Props {
  children: React.ReactElement;
  roles?: string[];
}

const ProtectedRoute: React.FC<Props> = ({ children, roles }) => {
  const { accessToken, roles: userRoles } = useAuth();

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.some((role) => userRoles.includes(role))) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;
