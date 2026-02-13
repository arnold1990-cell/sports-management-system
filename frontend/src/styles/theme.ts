import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: { main: '#2563eb' },
    secondary: { main: '#7c3aed' },
    background: { default: '#f3f6fb', paper: '#ffffff' },
    success: { main: '#16a34a' },
    warning: { main: '#d97706' },
    error: { main: '#dc2626' }
  },
  shape: { borderRadius: 12 },
  spacing: 8,
  typography: {
    fontFamily: 'Inter, Roboto, "Helvetica Neue", Arial, sans-serif',
    h4: { fontSize: '1.75rem', fontWeight: 700 },
    h5: { fontSize: '1.25rem', fontWeight: 700 },
    subtitle1: { fontSize: '1rem', fontWeight: 600 },
    body2: { color: '#64748b' },
    button: { textTransform: 'none', fontWeight: 600 }
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#ffffff',
          color: '#0f172a',
          boxShadow: '0 1px 1px rgba(15,23,42,0.06), 0 6px 20px rgba(15,23,42,0.04)'
        }
      }
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 1px 2px rgba(15,23,42,0.04), 0 8px 24px rgba(15,23,42,0.06)',
          border: '1px solid #e2e8f0'
        }
      }
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 10, paddingInline: 14 }
      }
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 700, color: '#334155', backgroundColor: '#f8fafc' }
      }
    }
  }
});

export default theme;
