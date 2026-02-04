import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert, MenuItem } from '@mui/material';
import api from '../api/client';

interface Team {
  id: string;
  name: string;
}

interface Player {
  id: string;
  firstName: string;
  lastName: string;
  position: string;
  status: string;
  teamName?: string;
}

const PlayersPage: React.FC = () => {
  const [players, setPlayers] = useState<Player[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [form, setForm] = useState({
    teamId: '',
    firstName: '',
    lastName: '',
    dob: '',
    position: '',
    jerseyNumber: '',
    status: 'ACTIVE',
    statsSummary: ''
  });
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      const [playersRes, teamsRes] = await Promise.all([
        api.get('/api/players'),
        api.get('/api/teams')
      ]);
      setPlayers(playersRes.data);
      setTeams(teamsRes.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load players');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreate = async () => {
    try {
      await api.post('/api/players', {
        ...form,
        teamId: form.teamId || null,
        jerseyNumber: form.jerseyNumber ? Number(form.jerseyNumber) : null,
        dob: form.dob
      });
      setForm({ teamId: '', firstName: '', lastName: '', dob: '', position: '', jerseyNumber: '', status: 'ACTIVE', statsSummary: '' });
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create player');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Players</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'grid', gap: 2 }}>
          <Typography variant="h6">Add Player</Typography>
          <TextField select label="Team" value={form.teamId} onChange={(e) => setForm({ ...form, teamId: e.target.value })}>
            <MenuItem value="">No team</MenuItem>
            {teams.map((team) => (
              <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>
            ))}
          </TextField>
          <TextField label="First Name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
          <TextField label="Last Name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          <TextField type="date" label="Date of Birth" InputLabelProps={{ shrink: true }} value={form.dob} onChange={(e) => setForm({ ...form, dob: e.target.value })} />
          <TextField label="Position" value={form.position} onChange={(e) => setForm({ ...form, position: e.target.value })} />
          <TextField label="Jersey Number" value={form.jerseyNumber} onChange={(e) => setForm({ ...form, jerseyNumber: e.target.value })} />
          <TextField select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            {['ACTIVE', 'INJURED', 'SUSPENDED'].map((status) => (
              <MenuItem key={status} value={status}>{status}</MenuItem>
            ))}
          </TextField>
          <TextField label="Stats Summary" value={form.statsSummary} onChange={(e) => setForm({ ...form, statsSummary: e.target.value })} />
          <Button variant="contained" onClick={handleCreate}>Save</Button>
        </CardContent>
      </Card>
      <Grid container spacing={2}>
        {players.map((player) => (
          <Grid item xs={12} md={4} key={player.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{player.firstName} {player.lastName}</Typography>
                <Typography variant="body2">Team: {player.teamName || 'Free agent'}</Typography>
                <Typography variant="body2">Position: {player.position}</Typography>
                <Typography variant="body2">Status: {player.status}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default PlayersPage;
