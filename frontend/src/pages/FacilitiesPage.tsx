import { Box, Button, Grid, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';
import api from '../api/client';

const FacilitiesPage: React.FC = () => {
  const [facilities, setFacilities] = useState<any[]>([]);
  const [name, setName] = useState('');
  const [pricePerHour, setPricePerHour] = useState('0');
  const load = async () => { const r = await api.get('/api/facilities'); setFacilities(r.data); };
  useEffect(() => { load(); }, []);
  const create = async () => { await api.post('/api/facilities', { name, pricePerHour, status: 'ACTIVE' }); setName(''); setPricePerHour('0'); load(); };

  return <Box>
    <Typography variant="h4" gutterBottom>Facilities</Typography>
    <Grid container spacing={2} sx={{ mb: 2 }}>
      <Grid item xs={6}><TextField fullWidth label="Name" value={name} onChange={e => setName(e.target.value)} /></Grid>
      <Grid item xs={3}><TextField fullWidth label="Price/hr" value={pricePerHour} onChange={e => setPricePerHour(e.target.value)} /></Grid>
      <Grid item xs={3}><Button fullWidth variant="contained" onClick={create}>Create</Button></Grid>
    </Grid>
    {facilities.map(f => <Typography key={f.id}>• {f.name} - {f.status} - {f.pricePerHour}</Typography>)}
  </Box>;
};

export default FacilitiesPage;
