import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography
} from '@mui/material';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Team { id: string; name: string }
interface Competition { id: string; name: string }
interface Season { id: string; name: string }
interface Fixture {
  id: string;
  homeTeamId: string;
  awayTeamId: string;
  homeTeamName: string;
  awayTeamName: string;
  competitionId: string;
  competitionName: string;
  seasonId: string;
  venue: string;
  matchDate: string;
  status: string;
  homeScore?: number;
  awayScore?: number;
}

const statusColor: Record<string, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  SCHEDULED: 'info',
  LIVE: 'warning',
  FINISHED: 'success',
  COMPLETED: 'success',
  POSTPONED: 'default',
  CANCELLED: 'error'
};

const FixturesPage: React.FC = () => {
  const { roles } = useAuth();
  const canManage = roles.some((role) => ['ADMIN', 'MANAGER', 'COACH'].includes(role));
  const [tab, setTab] = useState(0);
  const [fixtures, setFixtures] = useState<Fixture[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [seasons, setSeasons] = useState<Season[]>([]);
  const [selectedFixture, setSelectedFixture] = useState<Fixture | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState({ search: '', competitionId: '', teamId: '', from: '', to: '' });
  const [calendarView, setCalendarView] = useState<'month' | 'week' | 'day'>('month');
  const [calendarCursor, setCalendarCursor] = useState(new Date());

  const [form, setForm] = useState({
    homeTeamId: '',
    awayTeamId: '',
    competitionId: '',
    seasonId: '',
    venue: '',
    date: '',
    time: '',
    status: 'SCHEDULED',
    homeScore: '',
    awayScore: ''
  });

  const loadData = async () => {
    try {
      const [fixtureRes, teamRes, competitionRes, seasonRes] = await Promise.all([
        api.get('/api/fixtures/public'),
        api.get('/api/teams'),
        api.get('/api/competitions'),
        api.get('/api/competitions/seasons')
      ]);
      setFixtures(fixtureRes.data);
      setTeams(teamRes.data);
      setCompetitions(competitionRes.data);
      setSeasons(seasonRes.data);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load fixtures');
    }
  };

  useEffect(() => { loadData(); }, []);

  const createFixture = async () => {
    if (!form.homeTeamId || !form.awayTeamId || !form.competitionId || !form.seasonId || !form.venue || !form.date || !form.time) {
      setError('Please complete all required fixture fields.');
      return;
    }
    try {
      await api.post('/api/fixtures', {
        homeTeamId: form.homeTeamId,
        awayTeamId: form.awayTeamId,
        competitionId: form.competitionId,
        seasonId: form.seasonId,
        venue: form.venue,
        kickoffTime: `${form.date}T${form.time}:00`,
        status: form.status,
        homeScore: form.homeScore ? Number(form.homeScore) : null,
        awayScore: form.awayScore ? Number(form.awayScore) : null
      });
      setForm({ homeTeamId: '', awayTeamId: '', competitionId: '', seasonId: '', venue: '', date: '', time: '', status: 'SCHEDULED', homeScore: '', awayScore: '' });
      setError(null);
      setTab(1);
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to create fixture');
    }
  };

  const filteredFixtures = useMemo(() => fixtures.filter((fixture) => {
    const text = `${fixture.homeTeamName} ${fixture.awayTeamName} ${fixture.venue}`.toLowerCase();
    const matchSearch = text.includes(filters.search.toLowerCase());
    const matchCompetition = filters.competitionId ? fixture.competitionId === filters.competitionId : true;
    const matchTeam = filters.teamId ? fixture.homeTeamId === filters.teamId || fixture.awayTeamId === filters.teamId : true;
    const matchDate = new Date(fixture.matchDate);
    const fromOk = filters.from ? matchDate >= new Date(filters.from) : true;
    const toOk = filters.to ? matchDate <= new Date(`${filters.to}T23:59:59`) : true;
    return matchSearch && matchCompetition && matchTeam && fromOk && toOk;
  }), [fixtures, filters]);

  const upcomingFixtures = filteredFixtures.filter((item) => new Date(item.matchDate).getTime() >= Date.now());
  const results = filteredFixtures.filter((item) => ['FINISHED', 'COMPLETED'].includes(item.status));

  const calendarRange = useMemo(() => {
    const start = new Date(calendarCursor);
    const end = new Date(calendarCursor);
    if (calendarView === 'month') {
      start.setDate(1);
      end.setMonth(end.getMonth() + 1, 0);
    } else if (calendarView === 'week') {
      const day = start.getDay();
      start.setDate(start.getDate() - day);
      end.setDate(start.getDate() + 6);
    }
    return { start, end };
  }, [calendarCursor, calendarView]);

  const calendarFixtures = useMemo(() => fixtures.filter((fixture) => {
    const d = new Date(fixture.matchDate);
    return d >= calendarRange.start && d <= calendarRange.end;
  }), [fixtures, calendarRange]);

  const renderFixtureTable = (rows: Fixture[]) => (
    <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
      <Table size="small" stickyHeader>
        <TableHead>
          <TableRow>
            <TableCell>Fixture</TableCell>
            <TableCell>Date</TableCell>
            <TableCell>Competition</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Venue</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((fixture) => (
            <TableRow key={fixture.id} hover>
              <TableCell>{fixture.homeTeamName} vs {fixture.awayTeamName}</TableCell>
              <TableCell>{new Date(fixture.matchDate).toLocaleString()}</TableCell>
              <TableCell>{fixture.competitionName}</TableCell>
              <TableCell><Chip label={fixture.status} size="small" color={statusColor[fixture.status] || 'default'} /></TableCell>
              <TableCell>{fixture.venue}</TableCell>
              <TableCell>
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="outlined" onClick={() => setSelectedFixture(fixture)}>Edit</Button>
                  <Button size="small" variant="outlined">Update Score</Button>
                  <Button size="small" color="error" variant="text">Cancel</Button>
                </Stack>
              </TableCell>
            </TableRow>
          ))}
          {!rows.length && (
            <TableRow>
              <TableCell colSpan={6}><Typography color="text.secondary">No fixtures match the selected filters.</Typography></TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Fixtures</Typography>
      <Typography variant="body2" sx={{ mb: 2 }}>Create fixtures, manage upcoming matches, review results, and browse schedule calendar.</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Tabs value={tab} onChange={(_, value) => setTab(value)} variant="scrollable" scrollButtons="auto">
            <Tab label="Create Fixture" />
            <Tab label={`Upcoming Fixtures (${upcomingFixtures.length})`} />
            <Tab label={`Results (${results.length})`} />
            <Tab label="Calendar" />
          </Tabs>
        </CardContent>
      </Card>

      {tab === 0 && (
        <Card>
          <CardContent>
            {!canManage ? <Alert severity="info">You can view fixtures, but only managers/coaches/admin can create them.</Alert> : (
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}><TextField fullWidth select label="Home Team" helperText="Select home side" value={form.homeTeamId} onChange={(e) => setForm({ ...form, homeTeamId: e.target.value })}>{teams.map((team) => <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth select label="Away Team" helperText="Choose opponent" value={form.awayTeamId} onChange={(e) => setForm({ ...form, awayTeamId: e.target.value })}>{teams.map((team) => <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth type="date" label="Date" InputLabelProps={{ shrink: true }} value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} /></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth type="time" label="Time" InputLabelProps={{ shrink: true }} value={form.time} onChange={(e) => setForm({ ...form, time: e.target.value })} /></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth label="Venue" value={form.venue} helperText="Stadium or training field" onChange={(e) => setForm({ ...form, venue: e.target.value })} /></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth select label="Competition" value={form.competitionId} onChange={(e) => setForm({ ...form, competitionId: e.target.value })}>{competitions.map((competition) => <MenuItem key={competition.id} value={competition.id}>{competition.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth select label="Season" value={form.seasonId} onChange={(e) => setForm({ ...form, seasonId: e.target.value })}>{seasons.map((season) => <MenuItem key={season.id} value={season.id}>{season.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>{['SCHEDULED', 'LIVE', 'FINISHED', 'POSTPONED'].map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth label="Home Score" value={form.homeScore} onChange={(e) => setForm({ ...form, homeScore: e.target.value })} /></Grid>
                <Grid item xs={12} md={6}><TextField fullWidth label="Away Score" value={form.awayScore} onChange={(e) => setForm({ ...form, awayScore: e.target.value })} /></Grid>
                <Grid item xs={12}>
                  <Stack direction="row" spacing={1}>
                    <Button variant="contained" onClick={createFixture}>Save Fixture</Button>
                    <Button variant="outlined" onClick={() => setForm({ homeTeamId: '', awayTeamId: '', competitionId: '', seasonId: '', venue: '', date: '', time: '', status: 'SCHEDULED', homeScore: '', awayScore: '' })}>Reset</Button>
                  </Stack>
                </Grid>
              </Grid>
            )}
          </CardContent>
        </Card>
      )}

      {(tab === 1 || tab === 2) && (
        <Stack spacing={2}>
          <Card>
            <CardContent>
              <Grid container spacing={2}>
                <Grid item xs={12} md={4}><TextField fullWidth label="Search fixtures" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} /></Grid>
                <Grid item xs={12} md={2}><TextField fullWidth select label="Competition" value={filters.competitionId} onChange={(e) => setFilters({ ...filters, competitionId: e.target.value })}><MenuItem value="">All</MenuItem>{competitions.map((competition) => <MenuItem key={competition.id} value={competition.id}>{competition.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={2}><TextField fullWidth select label="Team" value={filters.teamId} onChange={(e) => setFilters({ ...filters, teamId: e.target.value })}><MenuItem value="">All</MenuItem>{teams.map((team) => <MenuItem key={team.id} value={team.id}>{team.name}</MenuItem>)}</TextField></Grid>
                <Grid item xs={12} md={2}><TextField fullWidth type="date" label="From" InputLabelProps={{ shrink: true }} value={filters.from} onChange={(e) => setFilters({ ...filters, from: e.target.value })} /></Grid>
                <Grid item xs={12} md={2}><TextField fullWidth type="date" label="To" InputLabelProps={{ shrink: true }} value={filters.to} onChange={(e) => setFilters({ ...filters, to: e.target.value })} /></Grid>
              </Grid>
              <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                {[filters.competitionId && 'Competition', filters.teamId && 'Team', filters.from && 'Date from', filters.to && 'Date to'].filter(Boolean).map((label) => (
                  <Chip key={label} label={label} size="small" color="primary" variant="outlined" />
                ))}
              </Stack>
            </CardContent>
          </Card>
          {tab === 1 ? renderFixtureTable(upcomingFixtures) : <><Button variant="contained" sx={{ alignSelf: 'flex-start' }}>Add Result</Button>{renderFixtureTable(results)}</>}
        </Stack>
      )}

      {tab === 3 && (
        <Card>
          <CardContent>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} alignItems={{ md: 'center' }} justifyContent="space-between" sx={{ mb: 2 }}>
              <Typography variant="h6">Calendar View</Typography>
              <Stack direction="row" spacing={1}>
                <Button variant="outlined" onClick={() => setCalendarCursor(new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() - 1, 1))}>Previous</Button>
                <Button variant="outlined" onClick={() => setCalendarCursor(new Date())}>Today</Button>
                <Button variant="outlined" onClick={() => setCalendarCursor(new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() + 1, 1))}>Next</Button>
                <FormControl size="small" sx={{ minWidth: 120 }}>
                  <InputLabel>View</InputLabel>
                  <Select label="View" value={calendarView} onChange={(e) => setCalendarView(e.target.value as 'month' | 'week' | 'day')}>
                    <MenuItem value="month">Month</MenuItem>
                    <MenuItem value="week">Week</MenuItem>
                    <MenuItem value="day">Day</MenuItem>
                  </Select>
                </FormControl>
              </Stack>
            </Stack>

            <Grid container spacing={2}>
              {calendarFixtures.map((fixture) => (
                <Grid item xs={12} md={6} lg={4} key={fixture.id}>
                  <Paper sx={{ p: 2, borderRadius: 2, cursor: 'pointer' }} onClick={() => setSelectedFixture(fixture)}>
                    <Typography fontWeight={700}>{fixture.homeTeamName} vs {fixture.awayTeamName}</Typography>
                    <Typography variant="body2">{new Date(fixture.matchDate).toLocaleString()}</Typography>
                    <Typography variant="body2">{fixture.venue}</Typography>
                    <Chip size="small" sx={{ mt: 1 }} label={fixture.status} color={statusColor[fixture.status] || 'default'} />
                  </Paper>
                </Grid>
              ))}
              {!calendarFixtures.length && <Grid item xs={12}><Alert severity="info">No fixtures in the visible range.</Alert></Grid>}
            </Grid>
          </CardContent>
        </Card>
      )}

      <Dialog open={!!selectedFixture} onClose={() => setSelectedFixture(null)} fullWidth>
        <DialogTitle>Fixture Details</DialogTitle>
        <DialogContent>
          {selectedFixture && (
            <Stack spacing={1}>
              <Typography fontWeight={700}>{selectedFixture.homeTeamName} vs {selectedFixture.awayTeamName}</Typography>
              <Typography>{new Date(selectedFixture.matchDate).toLocaleString()}</Typography>
              <Typography>Venue: {selectedFixture.venue}</Typography>
              <Typography>Competition: {selectedFixture.competitionName}</Typography>
              <Typography>Status: {selectedFixture.status}</Typography>
              {selectedFixture.homeScore !== undefined && <Typography>Score: {selectedFixture.homeScore} - {selectedFixture.awayScore}</Typography>}
            </Stack>
          )}
        </DialogContent>
      </Dialog>
    </Box>
  );
};

export default FixturesPage;
