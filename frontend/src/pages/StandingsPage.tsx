import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, MenuItem, TextField, Typography, Table, TableBody, TableCell, TableHead, TableRow, Alert } from '@mui/material';
import api from '../api/client';

interface Competition {
  id: string;
  name: string;
}

interface Season {
  id: string;
  name: string;
}

interface TeamStanding {
  teamId: string;
  teamName: string;
  played: number;
  won: number;
  drawn: number;
  lost: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  points: number;
}

const StandingsPage: React.FC = () => {
  const [competitions, setCompetitions] = useState<Competition[]>([]);
  const [seasons, setSeasons] = useState<Season[]>([]);
  const [competitionId, setCompetitionId] = useState('');
  const [seasonId, setSeasonId] = useState('');
  const [table, setTable] = useState<TeamStanding[]>([]);
  const [error, setError] = useState<string | null>(null);

  const loadOptions = async () => {
    const [competitionRes, seasonRes] = await Promise.all([
      api.get('/api/competitions'),
      api.get('/api/competitions/seasons')
    ]);
    setCompetitions(competitionRes.data);
    setSeasons(seasonRes.data);
    if (competitionRes.data.length) {
      setCompetitionId(competitionRes.data[0].id);
    }
    if (seasonRes.data.length) {
      setSeasonId(seasonRes.data[0].id);
    }
  };

  const loadStandings = async (compId: string, seaId: string) => {
    if (!compId || !seaId) return;
    try {
      const response = await api.get(`/api/standings/${compId}/${seaId}`);
      setTable(response.data.table);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load standings');
    }
  };

  useEffect(() => {
    loadOptions();
  }, []);

  useEffect(() => {
    if (competitionId && seasonId) {
      loadStandings(competitionId, seasonId);
    }
  }, [competitionId, seasonId]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Standings</Typography>
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
      {error && <Alert severity="error">{error}</Alert>}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Team</TableCell>
            <TableCell>Played</TableCell>
            <TableCell>Won</TableCell>
            <TableCell>Drawn</TableCell>
            <TableCell>Lost</TableCell>
            <TableCell>GF</TableCell>
            <TableCell>GA</TableCell>
            <TableCell>GD</TableCell>
            <TableCell>Pts</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {table.map((row) => (
            <TableRow key={row.teamId}>
              <TableCell>{row.teamName}</TableCell>
              <TableCell>{row.played}</TableCell>
              <TableCell>{row.won}</TableCell>
              <TableCell>{row.drawn}</TableCell>
              <TableCell>{row.lost}</TableCell>
              <TableCell>{row.goalsFor}</TableCell>
              <TableCell>{row.goalsAgainst}</TableCell>
              <TableCell>{row.goalDifference}</TableCell>
              <TableCell>{row.points}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  );
};

export default StandingsPage;
