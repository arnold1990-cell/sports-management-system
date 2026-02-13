import React from 'react';
import { Alert, Box, Button, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const UnauthorizedPage: React.FC = () => (
  <Box maxWidth={640} mx="auto">
    <Typography variant="h4" gutterBottom>
      Access denied
    </Typography>
    <Alert severity="warning" sx={{ mb: 2 }}>
      You do not have permission to perform this task.
    </Alert>
    <Button component={RouterLink} to="/" variant="contained">
      Go back home
    </Button>
  </Box>
);

export default UnauthorizedPage;
