import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert, MenuItem } from '@mui/material';
import api from '../api/client';

interface Club {
  id: string;
  name: string;
}

interface Team {
  id: string;
  name: string;
  clubName?: string;
  coachName?: string;
  homeGround?: string;
}

const TeamsPage: React.FC = () => {
  const [teams, setTeams] = useState<Team[]>([]);
  const [clubs, setClubs] = useState<Club[]>([]);
  const [name, setName] = useState('');
  const [clubId, setClubId] = useState('');
  const [coachName, setCoachName] = useState('');
  const [homeGround, setHomeGround] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      const [teamsRes, clubsRes] = await Promise.all([
        api.get('/api/teams'),
        api.get('/api/clubs')
      ]);
      setTeams(teamsRes.data);
      setClubs(clubsRes.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load teams');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreate = async () => {
    try {
      await api.post('/api/teams', { name, clubId: clubId || null, coachName, homeGround, logoUrl: null });
      setName('');
      setClubId('');
      setCoachName('');
      setHomeGround('');
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create team');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Teams</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'grid', gap: 2 }}>
          <Typography variant="h6">Create Team</Typography>
          <TextField label="Team Name" value={name} onChange={(e) => setName(e.target.value)} />
          <TextField select label="Club" value={clubId} onChange={(e) => setClubId(e.target.value)}>
            <MenuItem value="">No club</MenuItem>
            {clubs.map((club) => (
              <MenuItem key={club.id} value={club.id}>{club.name}</MenuItem>
            ))}
          </TextField>
          <TextField label="Coach" value={coachName} onChange={(e) => setCoachName(e.target.value)} />
          <TextField label="Home Ground" value={homeGround} onChange={(e) => setHomeGround(e.target.value)} />
          <Button variant="contained" onClick={handleCreate}>Save</Button>
        </CardContent>
      </Card>
      <Grid container spacing={2}>
        {teams.map((team) => (
          <Grid item xs={12} md={4} key={team.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{team.name}</Typography>
                <Typography variant="body2">Club: {team.clubName || 'Independent'}</Typography>
                <Typography variant="body2">Coach: {team.coachName || 'TBD'}</Typography>
                <Typography variant="body2">Ground: {team.homeGround || 'TBD'}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default TeamsPage;
