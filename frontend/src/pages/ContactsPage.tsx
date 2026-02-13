import { Alert, Box, Button, Grid, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';
import api from '../api/client';

const ContactsPage: React.FC = () => {
  const [headOffices, setHeadOffices] = useState<any[]>([]);
  const [clubContacts, setClubContacts] = useState<any[]>([]);
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    try {
      const [h, c] = await Promise.all([api.get('/api/contacts/head-offices'), api.get('/api/contacts/club-directory')]);
      setHeadOffices(h.data); setClubContacts(c.data);
    } catch (e: any) { setError(e?.response?.data?.message || 'Failed loading contacts'); }
  };
  useEffect(() => { load(); }, []);

  const create = async () => { await api.post('/api/contacts/head-offices', { name }); setName(''); load(); };

  return <Box>
    <Typography variant="h4" gutterBottom>Contacts</Typography>
    {error && <Alert severity="error">{error}</Alert>}
    <Grid container spacing={2} sx={{ mb: 2 }}>
      <Grid item xs={8}><TextField fullWidth label="Head Office Name" value={name} onChange={e => setName(e.target.value)} /></Grid>
      <Grid item xs={4}><Button fullWidth variant="contained" onClick={create}>Add Head Office</Button></Grid>
    </Grid>
    <Typography variant="h6">Head Offices</Typography>
    {headOffices.map(h => <Typography key={h.id}>• {h.name} ({h.region || 'N/A'})</Typography>)}
    <Typography variant="h6" sx={{ mt: 3 }}>Club Directory</Typography>
    {clubContacts.map(c => <Typography key={c.id}>• {c.adminName} - {c.adminEmail} ({c.adminPhone})</Typography>)}
  </Box>;
};

export default ContactsPage;
