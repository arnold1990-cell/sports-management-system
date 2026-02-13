import { Box, Button, Grid, MenuItem, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';
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
    <Typography variant="h4" gutterBottom>Subscriptions</Typography>
    <Grid container spacing={2} sx={{ mb: 2 }}>
      <Grid item xs={4}><TextField fullWidth label="Plan name" value={name} onChange={e => setName(e.target.value)} /></Grid>
      <Grid item xs={2}><TextField fullWidth label="Amount" value={amount} onChange={e => setAmount(e.target.value)} /></Grid>
      <Grid item xs={3}><TextField fullWidth select label="Period" value="MONTHLY"><MenuItem value="MONTHLY">Monthly</MenuItem></TextField></Grid>
      <Grid item xs={3}><Button variant="contained" fullWidth onClick={createPlan}>Create Plan</Button></Grid>
    </Grid>
    {plans.map(p => <Typography key={p.id}>• {p.name} - {p.amount} {p.currency} ({p.billingPeriod})</Typography>)}
  </Box>;
};

export default SubscriptionsPage;
