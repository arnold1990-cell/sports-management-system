import React from 'react';
import { Box, Card, CardContent, Typography, Grid, Button } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const HomePage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>Welcome to Sports Management System</Typography>
      <Typography variant="subtitle1" gutterBottom>
        Manage clubs, teams, fixtures, standings, and announcements in one platform.
      </Typography>
      <Grid container spacing={2} sx={{ mt: 2 }}>
        {[
          { title: 'Posts & News', link: '/posts' },
          { title: 'Fixtures & Results', link: '/fixtures' },
          { title: 'Standings', link: '/standings' }
        ].map((item) => (
          <Grid item xs={12} md={4} key={item.title}>
            <Card>
              <CardContent>
                <Typography variant="h6">{item.title}</Typography>
                <Button component={RouterLink} to={item.link} sx={{ mt: 1 }} variant="contained">
                  Open
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default HomePage;
