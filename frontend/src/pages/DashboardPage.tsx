import React, { useEffect, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Grid, Typography } from '@mui/material';
import api from '../api/client';

interface Summary {
  clubs: number;
  teams: number;
  players: number;
  fixtures: number;
  posts: number;
}

interface FixtureCard {
  id: string;
  homeTeam: string;
  awayTeam: string;
  matchDate: string;
  status: string;
  homeScore?: number;
  awayScore?: number;
}

interface DashboardResponse {
  summary: Summary;
  latestFixtures: FixtureCard[];
  latestPosts: string[];
}

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/dashboard');
      setData(response.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return <Typography>Loading dashboard...</Typography>;
  }

  if (error) {
    return (
      <Box>
        <Alert severity="error">{error}</Alert>
        <Button sx={{ mt: 2 }} variant="contained" onClick={loadData}>Retry</Button>
      </Box>
    );
  }

  if (!data) {
    return null;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard Overview</Typography>
      <Grid container spacing={2}>
        {Object.entries(data.summary).map(([key, value]) => (
          <Grid item xs={12} sm={6} md={2.4} key={key}>
            <Card>
              <CardContent>
                <Typography variant="subtitle2">{key.toUpperCase()}</Typography>
                <Typography variant="h5">{value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>Latest Fixtures</Typography>
        <Grid container spacing={2}>
          {data.latestFixtures.map((fixture) => (
            <Grid item xs={12} md={6} key={fixture.id}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle1">
                    {fixture.homeTeam} vs {fixture.awayTeam}
                  </Typography>
                  <Typography variant="body2">{new Date(fixture.matchDate).toLocaleString()}</Typography>
                  <Typography variant="body2">Status: {fixture.status}</Typography>
                  {fixture.homeScore !== null && fixture.homeScore !== undefined && (
                    <Typography variant="body2">Score: {fixture.homeScore} - {fixture.awayScore}</Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>Latest Posts</Typography>
        {data.latestPosts.map((post, index) => (
          <Typography key={index} variant="body2">• {post}</Typography>
        ))}
      </Box>
    </Box>
  );
};

export default DashboardPage;
