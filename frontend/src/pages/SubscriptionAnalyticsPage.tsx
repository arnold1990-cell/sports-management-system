import React, { useEffect, useMemo, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Grid, LinearProgress, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import api from '../api/client';

const toCsv = (rows: any[], headers: string[]) => {
  const line = [headers.join(',')];
  rows.forEach((row) => {
    line.push(headers.map((h) => JSON.stringify(String(row[h] ?? ''))).join(','));
  });
  return line.join('\n');
};

const downloadCsv = (name: string, content: string) => {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  a.click();
  URL.revokeObjectURL(url);
};

const SubscriptionAnalyticsPage: React.FC = () => {
  const [summary, setSummary] = useState<any>();
  const [revenue, setRevenue] = useState<any[]>([]);
  const [expiring, setExpiring] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      api.get('/api/analytics/subscriptions/summary'),
      api.get('/api/analytics/subscriptions/revenue'),
      api.get('/api/analytics/subscriptions/expiring?days=30')
    ])
      .then(([summaryRes, revenueRes, expiringRes]) => {
        setSummary(summaryRes.data);
        setRevenue(revenueRes.data);
        setExpiring(expiringRes.data);
      })
      .catch((e) => setError(e?.response?.data?.message || 'Unable to load analytics'));
  }, []);

  const maxRevenue = useMemo(() => Math.max(...revenue.map((item) => Number(item.revenue || 0)), 1), [revenue]);

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Subscription Analytics</Typography>
      {!summary ? <Typography>Loading analytics...</Typography> : (
        <>
          <Grid container spacing={2} sx={{ mb: 2 }}>
            {[{ label: 'Revenue This Month', value: summary.revenueThisMonth }, { label: 'Active Subscriptions', value: summary.activeSubscriptions }, { label: 'Expired Subscriptions', value: summary.expiredSubscriptions }, { label: 'Pending Payments', value: summary.pendingPayments }].map((card) => (
              <Grid item xs={12} sm={6} lg={3} key={card.label}><Card><CardContent><Typography color="text.secondary">{card.label}</Typography><Typography variant="h5">{card.value}</Typography></CardContent></Card></Grid>
            ))}
          </Grid>

          <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid item xs={12} lg={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Monthly Revenue (Line)</Typography>
                  <Stack spacing={1.2}>
                    {revenue.map((item) => (
                      <Box key={item.month}><Typography variant="body2">{item.month} — ${item.revenue}</Typography><LinearProgress variant="determinate" value={(Number(item.revenue) / maxRevenue) * 100} sx={{ height: 8, borderRadius: 4 }} /></Box>
                    ))}
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} lg={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Subscriptions by Type (Bar)</Typography>
                  <Stack spacing={1.2}>{summary.byType.map((item: any) => <Box key={item.type}><Typography variant="body2">{item.type} ({item.count})</Typography><LinearProgress variant="determinate" value={(item.count / Math.max(...summary.byType.map((entry: any) => entry.count), 1)) * 100} /></Box>)}</Stack>
                  <Typography variant="h6" sx={{ mt: 2 }} gutterBottom>Status Mix (Pie)</Typography>
                  <Stack direction="row" spacing={1.2} flexWrap="wrap">{summary.statusMix.map((item: any) => <Button key={item.status} size="small" variant="outlined">{item.status}: {item.count}</Button>)}</Stack>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          <Grid container spacing={2}>
            <Grid item xs={12} lg={4}>
              <Card><CardContent><Stack direction="row" justifyContent="space-between"><Typography variant="h6">Top Paying Clubs</Typography><Button size="small" onClick={() => downloadCsv('top-paying-clubs.csv', toCsv(summary.topPayingClubs, ['club', 'totalPaid']))}>Export CSV</Button></Stack><TableContainer><Table size="small"><TableHead><TableRow><TableCell>Club</TableCell><TableCell>Total Paid</TableCell></TableRow></TableHead><TableBody>{summary.topPayingClubs.map((club: any) => <TableRow key={club.club}><TableCell>{club.club}</TableCell><TableCell>{club.totalPaid}</TableCell></TableRow>)}</TableBody></Table></TableContainer></CardContent></Card>
            </Grid>
            <Grid item xs={12} lg={4}>
              <Card><CardContent><Stack direction="row" justifyContent="space-between"><Typography variant="h6">Expiring Soon (30d)</Typography><Button size="small" onClick={() => downloadCsv('expiring-soon.csv', toCsv(expiring, ['subscriptionId', 'status', 'endDate']))}>Export CSV</Button></Stack><TableContainer><Table size="small"><TableHead><TableRow><TableCell>Subscription</TableCell><TableCell>Status</TableCell><TableCell>End Date</TableCell></TableRow></TableHead><TableBody>{expiring.map((item: any) => <TableRow key={item.subscriptionId}><TableCell>{String(item.subscriptionId).slice(0, 8)}</TableCell><TableCell>{item.status}</TableCell><TableCell>{item.endDate}</TableCell></TableRow>)}</TableBody></Table></TableContainer></CardContent></Card>
            </Grid>
            <Grid item xs={12} lg={4}>
              <Card><CardContent><Stack direction="row" justifyContent="space-between"><Typography variant="h6">High Risk / Late Payments</Typography><Button size="small" onClick={() => downloadCsv('high-risk.csv', toCsv(summary.highRisk, ['subscriptionId', 'riskScore', 'overdueDays']))}>Export CSV</Button></Stack><TableContainer><Table size="small"><TableHead><TableRow><TableCell>Subscription</TableCell><TableCell>Risk</TableCell><TableCell>Overdue Days</TableCell></TableRow></TableHead><TableBody>{summary.highRisk.map((item: any) => <TableRow key={item.subscriptionId}><TableCell>{String(item.subscriptionId).slice(0, 8)}</TableCell><TableCell>{item.riskScore}</TableCell><TableCell>{item.overdueDays}</TableCell></TableRow>)}</TableBody></Table></TableContainer></CardContent></Card>
            </Grid>
          </Grid>
        </>
      )}
    </Box>
  );
};

export default SubscriptionAnalyticsPage;
