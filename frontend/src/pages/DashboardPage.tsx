import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Grid, Typography } from '@mui/material';
import api from '../api/client';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<any>();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get('/api/dashboard').then(r => setData(r.data)).catch(e => setError(e?.response?.data?.message || 'Failed to load'));
  }, []);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (!data) return <Typography>Loading dashboard...</Typography>;

  return <Box>
    <Typography variant="h4" gutterBottom>Modern Dashboard</Typography>
    <Grid container spacing={2}>
      {Object.entries(data.summary).map(([k, v]) => <Grid item xs={12} sm={6} md={3} key={k}><Card><CardContent><Typography variant="subtitle2">{k}</Typography><Typography variant="h5">{String(v)}</Typography></CardContent></Card></Grid>)}
    </Grid>

    <Typography variant="h6" sx={{ mt: 3 }}>Upcoming Matches (7 days)</Typography>
    {data.upcomingMatches.map((m: any) => <Typography key={m.id}>• {m.homeTeam} vs {m.awayTeam} ({new Date(m.matchDate).toLocaleString()})</Typography>)}

    <Typography variant="h6" sx={{ mt: 3 }}>Latest Payments</Typography>
    {data.latestPayments.map((p: any) => <Typography key={p.id}>• {p.amount} {p.currency} - {p.status}</Typography>)}

    <Typography variant="h6" sx={{ mt: 3 }}>Bookings</Typography>
    <Typography>Today: {data.bookingSummary.today} | This Week: {data.bookingSummary.thisWeek}</Typography>
    <Typography>Unread Messages + Notifications: {data.unreadMessages}</Typography>
  </Box>;
};

export default DashboardPage;
