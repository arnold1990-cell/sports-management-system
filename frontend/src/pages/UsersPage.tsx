import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, TextField, Typography, Grid, MenuItem, Alert, CircularProgress } from '@mui/material';
import api from '../api/client';

interface User {
  id: string;
  email: string;
  fullName: string;
  roles: string[];
}

const roleOptions = ['ADMIN', 'MANAGER', 'COACH', 'PLAYER', 'REFEREE', 'VIEWER'];

const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/users');
      setUsers(response.data);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const updateRoles = async (userId: string, roles: string[]) => {
    try {
      await api.put(`/api/users/${userId}/roles`, { roles });
      await loadUsers();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to update roles');
    }
  };

  const toRolesArray = (value: string | string[]) => (Array.isArray(value) ? value : value.split(','));

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Manage Users</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {loading && <CircularProgress size={24} sx={{ mb: 2 }} />}
      <Grid container spacing={2}>
        {users.map((user) => (
          <Grid item xs={12} md={6} key={user.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{user.fullName}</Typography>
                <Typography variant="body2">{user.email}</Typography>
                <TextField
                  select
                  label="Roles"
                  SelectProps={{ multiple: true }}
                  value={user.roles}
                  onChange={(e) => updateRoles(user.id, toRolesArray(e.target.value))}
                  sx={{ mt: 2, minWidth: 240 }}
                >
                  {roleOptions.map((role) => (
                    <MenuItem key={role} value={role}>{role}</MenuItem>
                  ))}
                </TextField>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default UsersPage;
