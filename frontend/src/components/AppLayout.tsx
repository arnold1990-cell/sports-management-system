import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box, Container } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { accessToken, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }} component={RouterLink} to="/" color="inherit">
            SportsMS
          </Typography>
          <Button color="inherit" component={RouterLink} to="/posts">Posts</Button>
          <Button color="inherit" component={RouterLink} to="/fixtures">Fixtures</Button>
          <Button color="inherit" component={RouterLink} to="/standings">Standings</Button>
          {accessToken && (
            <Button color="inherit" component={RouterLink} to="/dashboard">Dashboard</Button>
          )}
          {hasRole('ADMIN') && (
            <Button color="inherit" component={RouterLink} to="/admin/posts">Admin Posts</Button>
          )}
          {hasRole('ADMIN') && (
            <Button color="inherit" component={RouterLink} to="/admin/users">Users</Button>
          )}
          {!accessToken ? (
            <Button color="inherit" component={RouterLink} to="/login">Login</Button>
          ) : (
            <Button color="inherit" onClick={handleLogout}>Logout</Button>
          )}
        </Toolbar>
      </AppBar>
      <Container sx={{ py: 4 }}>{children}</Container>
    </Box>
  );
};

export default AppLayout;
