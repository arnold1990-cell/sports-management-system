import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface Props {
  children: React.ReactElement;
  roles: string[];
}

const RoleRoute: React.FC<Props> = ({ children, roles }) => {
  const { isAuthenticated, roles: userRoles } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!roles.some((role) => userRoles.includes(role))) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default RoleRoute;
