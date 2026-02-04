import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert } from '@mui/material';
import api from '../api/client';

interface Club {
  id: string;
  name: string;
  city?: string;
}

const ClubsPage: React.FC = () => {
  const [clubs, setClubs] = useState<Club[]>([]);
  const [name, setName] = useState('');
  const [city, setCity] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadClubs = async () => {
    const response = await api.get('/api/clubs');
    setClubs(response.data);
  };

  useEffect(() => {
    loadClubs();
  }, []);

  const handleCreate = async () => {
    try {
      await api.post('/api/clubs', { name, city });
      setName('');
      setCity('');
      loadClubs();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create club');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Clubs</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'grid', gap: 2 }}>
          <Typography variant="h6">Create Club</Typography>
          <TextField label="Club Name" value={name} onChange={(e) => setName(e.target.value)} />
          <TextField label="City" value={city} onChange={(e) => setCity(e.target.value)} />
          <Button variant="contained" onClick={handleCreate}>Save</Button>
        </CardContent>
      </Card>
      <Grid container spacing={2}>
        {clubs.map((club) => (
          <Grid item xs={12} md={4} key={club.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{club.name}</Typography>
                <Typography variant="body2">{club.city || 'No city provided'}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default ClubsPage;
