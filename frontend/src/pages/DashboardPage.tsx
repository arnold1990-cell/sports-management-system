import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  LinearProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography
} from '@mui/material';
import api from '../api/client';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<any>();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get('/api/dashboard').then((r) => setData(r.data)).catch((e) => setError(e?.response?.data?.message || 'Failed to load'));
  }, []);

  const statusPie = useMemo(() => {
    if (!data?.subscriptionStatuses?.length) {
      return [];
    }
    const total = data.subscriptionStatuses.reduce((acc: number, item: any) => acc + Number(item.value || 0), 0) || 1;
    return data.subscriptionStatuses.map((item: any) => ({ ...item, pct: Math.round((Number(item.value) / total) * 100) }));
  }, [data]);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (!data) return <Typography>Loading dashboard...</Typography>;

  const stats = [
    { label: 'Total Clubs', value: data.summary.clubs },
    { label: 'Total Teams', value: data.summary.teams },
    { label: 'Total Players', value: data.summary.players },
    { label: 'Upcoming Matches (7d)', value: data.upcomingMatches.length },
    { label: 'Active Subscriptions', value: data.subscriptionStatuses.find((s: any) => s.label === 'ACTIVE')?.value || 0 },
    { label: 'Pending Facility Bookings', value: data.bookingSummary.thisWeek },
    { label: 'Unread Chat + Notifications', value: data.unreadMessages }
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Overview Dashboard</Typography>
      <Typography variant="body2" sx={{ mb: 2 }}>Live operations snapshot with quick insights across clubs, fixtures, subscriptions, and communications.</Typography>

      <Grid container spacing={2} sx={{ mb: 2 }}>
        {stats.map((item) => (
          <Grid item xs={12} sm={6} lg={3} key={item.label}>
            <Card><CardContent><Typography color="text.secondary" variant="body2">{item.label}</Typography><Typography variant="h5">{item.value}</Typography></CardContent></Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={12} lg={7}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Upcoming Fixtures</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead><TableRow><TableCell>Match</TableCell><TableCell>Date</TableCell><TableCell>Status</TableCell></TableRow></TableHead>
                  <TableBody>
                    {data.upcomingMatches.map((m: any) => (
                      <TableRow key={m.id}>
                        <TableCell>{m.homeTeam} vs {m.awayTeam}</TableCell>
                        <TableCell>{new Date(m.matchDate).toLocaleString()}</TableCell>
                        <TableCell><Chip label={m.status} size="small" color={m.status === 'LIVE' ? 'warning' : 'info'} /></TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Latest Subscription Payments</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead><TableRow><TableCell>Amount</TableCell><TableCell>Status</TableCell><TableCell>Paid At</TableCell></TableRow></TableHead>
                  <TableBody>
                    {data.latestPayments.map((p: any) => (
                      <TableRow key={p.id}><TableCell>{p.amount} {p.currency}</TableCell><TableCell>{p.status}</TableCell><TableCell>{p.paidAt ? new Date(p.paidAt).toLocaleString() : 'Pending'}</TableCell></TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} lg={5}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Revenue per Month</Typography>
              <Stack spacing={1.5}>
                {data.revenuePerMonth.map((row: any) => {
                  const max = Math.max(...data.revenuePerMonth.map((r: any) => Number(r.value || 0)), 1);
                  return (
                    <Box key={row.label}>
                      <Stack direction="row" justifyContent="space-between"><Typography variant="body2">{row.label}</Typography><Typography variant="body2">${row.value}</Typography></Stack>
                      <LinearProgress variant="determinate" value={(Number(row.value) / max) * 100} sx={{ height: 8, borderRadius: 4 }} />
                    </Box>
                  );
                })}
              </Stack>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Active vs Expired</Typography>
              <Stack spacing={1}>
                {statusPie.map((item: any) => (
                  <Box key={item.label}>
                    <Stack direction="row" justifyContent="space-between"><Typography>{item.label}</Typography><Typography>{item.value} ({item.pct}%)</Typography></Stack>
                    <LinearProgress value={item.pct} variant="determinate" color={item.label === 'ACTIVE' ? 'success' : item.label === 'EXPIRED' ? 'warning' : 'error'} sx={{ height: 8, borderRadius: 4 }} />
                  </Box>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;
