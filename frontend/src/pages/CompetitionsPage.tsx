import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert, MenuItem } from '@mui/material';
import api from '../api/client';

interface Season {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
}

interface Team {
  id: string;
  name: string;
}

interface Competition {
  id: string;
  name: string;
  type: string;
  teamIds: string[];
}

const CompetitionsPage: React.FC = () => {
  const [seasons, setSeasons] = useState<Season[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [seasonForm, setSeasonForm] = useState({ name: '', startDate: '', endDate: '' });
  const [competitionForm, setCompetitionForm] = useState({ name: '', type: 'LEAGUE', teamIds: [] as string[] });
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    const [seasonRes, teamRes, competitionRes] = await Promise.all([
      api.get('/api/competitions/seasons'),
      api.get('/api/teams'),
      api.get('/api/competitions')
    ]);
    setSeasons(seasonRes.data);
    setTeams(teamRes.data);
    setCompetitions(competitionRes.data);
  };

  useEffect(() => {
    loadData();
  }, []);

  const createSeason = async () => {
    try {
      await api.post('/api/competitions/seasons', seasonForm);
      setSeasonForm({ name: '', startDate: '', endDate: '' });
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create season');
    }
  };

  const createCompetition = async () => {
    try {
      await api.post('/api/competitions', competitionForm);
      setCompetitionForm({ name: '', type: 'LEAGUE', teamIds: [] });
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create competition');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Seasons & Competitions</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              <Typography variant="h6">Create Season</Typography>
              <TextField label="Season Name" value={seasonForm.name} onChange={(e) => setSeasonForm({ ...seasonForm, name: e.target.value })} />
              <TextField type="date" label="Start Date" InputLabelProps={{ shrink: true }} value={seasonForm.startDate} onChange={(e) => setSeasonForm({ ...seasonForm, startDate: e.target.value })} />
              <TextField type="date" label="End Date" InputLabelProps={{ shrink: true }} value={seasonForm.endDate} onChange={(e) => setSeasonForm({ ...seasonForm, endDate: e.target.value })} />
              <Button variant="contained" onClick={createSeason}>Save</Button>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              <Typography variant="h6">Create Competition</Typography>
              <TextField label="Name" value={competitionForm.name} onChange={(e) => setCompetitionForm({ ...competitionForm, name: e.target.value })} />
              <TextField select label="Type" value={competitionForm.type} onChange={(e) => setCompetitionForm({ ...competitionForm, type: e.target.value })}>
                {['LEAGUE', 'CUP'].map((type) => (
                  <MenuItem key={type} value={type}>{type}</MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Teams"
                SelectProps={{ multiple: true }}
                value={competitionForm.teamIds}
                onChange={(e) => setCompetitionForm({ ...competitionForm, teamIds: e.target.value as string[] })}
              >
                {teams.map((team) => (
                  <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>
                ))}
              </TextField>
              <Button variant="contained" onClick={createCompetition}>Save</Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>Seasons</Typography>
        <Grid container spacing={2}>
          {seasons.map((season) => (
            <Grid item xs={12} md={4} key={season.id}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle1">{season.name}</Typography>
                  <Typography variant="body2">{season.startDate} - {season.endDate}</Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>Competitions</Typography>
        <Grid container spacing={2}>
          {competitions.map((competition) => (
            <Grid item xs={12} md={4} key={competition.id}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle1">{competition.name}</Typography>
                  <Typography variant="body2">Type: {competition.type}</Typography>
                  <Typography variant="body2">Teams: {competition.teamIds.length}</Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Box>
  );
};

export default CompetitionsPage;
