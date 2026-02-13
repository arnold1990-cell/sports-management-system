import { Box, Button, Card, CardContent, Grid, MenuItem, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import api from '../api/client';

const SubscriptionsPage: React.FC = () => {
  const [plans, setPlans] = useState<any[]>([]);
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('0');

  const load = async () => { const p = await api.get('/api/subscriptions/plans'); setPlans(p.data); };
  useEffect(() => { load(); }, []);

  const createPlan = async () => {
    await api.post('/api/subscriptions/plans', { name, type: 'Club membership', amount, currency: 'USD', billingPeriod: 'MONTHLY', graceDays: 7, active: true });
    setName(''); setAmount('0'); load();
  };

  return <Box>
    <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" sx={{ mb: 2 }}>
      <Typography variant="h4" gutterBottom>Subscriptions</Typography>
      <Button variant="outlined" component={RouterLink} to="/subscriptions/analytics">Open Analytics</Button>
    </Stack>

    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Grid container spacing={2}>
          <Grid item xs={12} md={4}><TextField fullWidth label="Plan name" value={name} onChange={e => setName(e.target.value)} /></Grid>
          <Grid item xs={12} md={2}><TextField fullWidth label="Amount" value={amount} onChange={e => setAmount(e.target.value)} /></Grid>
          <Grid item xs={12} md={3}><TextField fullWidth select label="Period" value="MONTHLY"><MenuItem value="MONTHLY">Monthly</MenuItem></TextField></Grid>
          <Grid item xs={12} md={3}><Button variant="contained" fullWidth onClick={createPlan}>Create Plan</Button></Grid>
        </Grid>
      </CardContent>
    </Card>

    <TableContainer component={Card}>
      <Table>
        <TableHead><TableRow><TableCell>Plan</TableCell><TableCell>Type</TableCell><TableCell>Amount</TableCell><TableCell>Billing</TableCell></TableRow></TableHead>
        <TableBody>
          {plans.map((plan) => <TableRow key={plan.id}><TableCell>{plan.name}</TableCell><TableCell>{plan.type}</TableCell><TableCell>{plan.amount} {plan.currency}</TableCell><TableCell>{plan.billingPeriod}</TableCell></TableRow>)}
        </TableBody>
      </Table>
    </TableContainer>
  </Box>;
};

export default SubscriptionsPage;
