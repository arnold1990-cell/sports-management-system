import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert, MenuItem } from '@mui/material';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Team {
  id: string;
  name: string;
}

interface Competition {
  id: string;
  name: string;
}

interface Season {
  id: string;
  name: string;
}

interface Fixture {
  id: string;
  homeTeamName: string;
  awayTeamName: string;
  matchDate: string;
  venue: string;
  status: string;
  homeScore?: number;
  awayScore?: number;
}

const FixturesPage: React.FC = () => {
  const { roles } = useAuth();
  const canManage = roles.some((role) => ['ADMIN', 'MANAGER', 'COACH'].includes(role));
  const [fixtures, setFixtures] = useState<Fixture[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [seasons, setSeasons] = useState<Season[]>([]);
  const [competitionId, setCompetitionId] = useState('');
  const [seasonId, setSeasonId] = useState('');
  const [form, setForm] = useState({
    homeTeamId: '',
    awayTeamId: '',
    competitionId: '',
    seasonId: '',
    venue: '',
    matchDate: '',
    status: 'SCHEDULED',
    homeScore: '',
    awayScore: ''
  });
  const [error, setError] = useState<string | null>(null);

  const loadOptions = async () => {
    try {
      const [teamRes, competitionRes, seasonRes] = await Promise.all([
        api.get('/api/teams'),
        api.get('/api/competitions'),
        api.get('/api/competitions/seasons')
      ]);
      setTeams(teamRes.data);
      setCompetitions(competitionRes.data);
      setSeasons(seasonRes.data);
      if (competitionRes.data.length) {
        setCompetitionId(competitionRes.data[0].id);
      }
      if (seasonRes.data.length) {
        setSeasonId(seasonRes.data[0].id);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load filters');
    }
  };

  const loadFixtures = async (compId: string, seaId: string) => {
    if (!compId || !seaId) return;
    try {
      const fixtureRes = await api.get('/api/fixtures/public', {
        params: {
          competitionId: compId,
          seasonId: seaId
        }
      });
      setFixtures(fixtureRes.data);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load fixtures');
    }
  };

  useEffect(() => {
    loadOptions();
  }, []);

  useEffect(() => {
    if (competitionId && seasonId) {
      loadFixtures(competitionId, seasonId);
    }
  }, [competitionId, seasonId]);

  const createFixture = async () => {
    try {
      await api.post('/api/fixtures', {
        homeTeamId: form.homeTeamId,
        awayTeamId: form.awayTeamId,
        competitionId: form.competitionId,
        seasonId: form.seasonId,
        venue: form.venue,
        kickoffTime: form.matchDate,
        status: form.status,
        homeScore: form.homeScore ? Number(form.homeScore) : null,
        awayScore: form.awayScore ? Number(form.awayScore) : null
      });
      setForm({
        homeTeamId: '',
        awayTeamId: '',
        competitionId: '',
        seasonId: '',
        venue: '',
        matchDate: '',
        status: 'SCHEDULED',
        homeScore: '',
        awayScore: ''
      });
      if (competitionId && seasonId) {
        loadFixtures(competitionId, seasonId);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create fixture');
    }
  };

  const upcomingFixtures = fixtures
    .filter((fixture) => fixture.status !== 'FINISHED')
    .sort((a, b) => new Date(a.matchDate).getTime() - new Date(b.matchDate).getTime());

  const results = fixtures
    .filter((fixture) => fixture.status === 'FINISHED')
    .sort((a, b) => new Date(b.matchDate).getTime() - new Date(a.matchDate).getTime());

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Fixtures & Results</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField select label="Competition" value={competitionId} onChange={(e) => setCompetitionId(e.target.value)}>
            {competitions.map((competition) => (
              <MenuItem key={competition.id} value={competition.id}>{competition.name}</MenuItem>
            ))}
          </TextField>
          <TextField select label="Season" value={seasonId} onChange={(e) => setSeasonId(e.target.value)}>
            {seasons.map((season) => (
              <MenuItem key={season.id} value={season.id}>{season.name}</MenuItem>
            ))}
          </TextField>
        </CardContent>
      </Card>
      {canManage && (
        <Card sx={{ mb: 3 }}>
          <CardContent sx={{ display: 'grid', gap: 2 }}>
            <Typography variant="h6">Create Fixture</Typography>
            <TextField select label="Home Team" value={form.homeTeamId} onChange={(e) => setForm({ ...form, homeTeamId: e.target.value })}>
              {teams.map((team) => (
                <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>
              ))}
            </TextField>
            <TextField select label="Away Team" value={form.awayTeamId} onChange={(e) => setForm({ ...form, awayTeamId: e.target.value })}>
              {teams.map((team) => (
                <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>
              ))}
            </TextField>
            <TextField select label="Competition" value={form.competitionId} onChange={(e) => setForm({ ...form, competitionId: e.target.value })}>
              {competitions.map((competition) => (
                <MenuItem key={competition.id} value={competition.id}>{competition.name}</MenuItem>
              ))}
            </TextField>
            <TextField select label="Season" value={form.seasonId} onChange={(e) => setForm({ ...form, seasonId: e.target.value })}>
              {seasons.map((season) => (
                <MenuItem key={season.id} value={season.id}>{season.name}</MenuItem>
              ))}
            </TextField>
            <TextField label="Venue" value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} />
            <TextField type="datetime-local" label="Match Date" InputLabelProps={{ shrink: true }} value={form.matchDate} onChange={(e) => setForm({ ...form, matchDate: e.target.value })} />
            <TextField select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {['SCHEDULED', 'LIVE', 'FINISHED', 'POSTPONED'].map((status) => (
                <MenuItem key={status} value={status}>{status}</MenuItem>
              ))}
            </TextField>
            <TextField label="Home Score" value={form.homeScore} onChange={(e) => setForm({ ...form, homeScore: e.target.value })} />
            <TextField label="Away Score" value={form.awayScore} onChange={(e) => setForm({ ...form, awayScore: e.target.value })} />
            <Button variant="contained" onClick={createFixture}>Save</Button>
          </CardContent>
        </Card>
      )}
      <Typography variant="h6" gutterBottom>Upcoming Fixtures</Typography>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {upcomingFixtures.map((fixture) => (
          <Grid item xs={12} md={6} key={fixture.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{fixture.homeTeamName} vs {fixture.awayTeamName}</Typography>
                <Typography variant="body2">{new Date(fixture.matchDate).toLocaleString()}</Typography>
                <Typography variant="body2">Venue: {fixture.venue}</Typography>
                <Typography variant="body2">Status: {fixture.status}</Typography>
                {fixture.homeScore !== null && fixture.homeScore !== undefined && fixture.awayScore !== null && fixture.awayScore !== undefined && (
                  <Typography variant="body2">Score: {fixture.homeScore} - {fixture.awayScore}</Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
        {!upcomingFixtures.length && (
          <Grid item xs={12}>
            <Alert severity="info">No upcoming fixtures for the selected competition and season.</Alert>
          </Grid>
        )}
      </Grid>
      <Typography variant="h6" gutterBottom>Results</Typography>
      <Grid container spacing={2}>
        {results.map((fixture) => (
          <Grid item xs={12} md={6} key={fixture.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{fixture.homeTeamName} vs {fixture.awayTeamName}</Typography>
                <Typography variant="body2">{new Date(fixture.matchDate).toLocaleString()}</Typography>
                <Typography variant="body2">Venue: {fixture.venue}</Typography>
                <Typography variant="body2">Status: {fixture.status}</Typography>
                {fixture.homeScore !== null && fixture.homeScore !== undefined && fixture.awayScore !== null && fixture.awayScore !== undefined && (
                  <Typography variant="body2">Score: {fixture.homeScore} - {fixture.awayScore}</Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
        {!results.length && (
          <Grid item xs={12}>
            <Alert severity="info">No results recorded for the selected competition and season.</Alert>
          </Grid>
        )}
      </Grid>
    </Box>
  );
};

export default FixturesPage;
