import React from 'react';
import {
  AppBar,
  Avatar,
  Badge,
  Box,
  Button,
  Chip,
  Collapse,
  Container,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Tooltip,
  Typography
} from '@mui/material';
import {
  Apartment,
  Article,
  Dashboard,
  EmojiEvents,
  ExpandLess,
  ExpandMore,
  Groups,
  Home,
  Menu as MenuIcon,
  Notifications,
  People,
  Person,
  Sports,
  SportsSoccer,
  Stadium,
  Subscriptions,
  TableChart,
  Timeline
} from '@mui/icons-material';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';

type NavItem = {
  label: string;
  path: string;
  icon: React.ReactNode;
  visible: boolean;
};

type NavGroup = {
  label: string;
  icon: React.ReactNode;
  items: NavItem[];
};

type NotificationItem = {
  id: string;
  title: string;
  message: string;
  readAt: string | null;
};

const drawerWidth = 270;

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, logout, hasRole, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const navGroups: NavGroup[] = [
    {
      label: 'Dashboard',
      icon: <Dashboard fontSize="small" />,
      items: [
        { label: 'Overview', path: '/dashboard', icon: <Dashboard fontSize="small" />, visible: isAuthenticated },
        {
          label: 'Analytics',
          path: '/subscriptions/analytics',
          icon: <Timeline fontSize="small" />,
          visible: hasRole('ADMIN') || hasRole('MANAGER')
        }
      ]
    },
    {
      label: 'Sports',
      icon: <SportsSoccer fontSize="small" />,
      items: [
        { label: 'Fixtures', path: '/fixtures', icon: <Sports fontSize="small" />, visible: true },
        { label: 'Standings', path: '/standings', icon: <TableChart fontSize="small" />, visible: true },
        { label: 'Competitions', path: '/competitions', icon: <EmojiEvents fontSize="small" />, visible: hasRole('ADMIN') }
      ]
    },
    {
      label: 'Management',
      icon: <Groups fontSize="small" />,
      items: [
        { label: 'Clubs', path: '/clubs', icon: <Apartment fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') },
        { label: 'Teams', path: '/teams', icon: <Groups fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') || hasRole('COACH') },
        { label: 'Players', path: '/players', icon: <Person fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') || hasRole('COACH') },
        { label: 'Contacts', path: '/contacts', icon: <People fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') }
      ]
    },
    {
      label: 'Admin',
      icon: <Stadium fontSize="small" />,
      items: [
        { label: 'Facilities', path: '/facilities', icon: <Stadium fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') || hasRole('COACH') },
        { label: 'Subscriptions', path: '/subscriptions', icon: <Subscriptions fontSize="small" />, visible: hasRole('ADMIN') || hasRole('MANAGER') },
        { label: 'Users', path: '/admin/users', icon: <People fontSize="small" />, visible: hasRole('ADMIN') },
        { label: 'Admin Posts', path: '/admin/posts', icon: <Article fontSize="small" />, visible: hasRole('ADMIN') }
      ]
    }
  ];

  const [mobileOpen, setMobileOpen] = React.useState(false);
  const [openGroups, setOpenGroups] = React.useState<Record<string, boolean>>({ Sports: true, Dashboard: true });
  const [profileAnchor, setProfileAnchor] = React.useState<null | HTMLElement>(null);
  const [notificationAnchor, setNotificationAnchor] = React.useState<null | HTMLElement>(null);
  const [notifications, setNotifications] = React.useState<NotificationItem[]>([]);

  React.useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      return;
    }
    api
      .get('/api/notifications')
      .then((res) => {
        const mapped = (res.data as any[]).map((item) => ({
          id: String(item.id),
          title: String(item.title),
          message: String(item.message),
          readAt: item.readAt ? String(item.readAt) : null
        }));
        setNotifications(mapped);
      })
      .catch(() => setNotifications([]));
  }, [isAuthenticated]);

  const unreadCount = notifications.filter((item) => !item.readAt).length;

  const toggleGroup = (group: string) => {
    setOpenGroups((prev) => ({ ...prev, [group]: !prev[group] }));
  };

  const handleLogout = async () => {
    await logout();
    setProfileAnchor(null);
    navigate('/login');
  };

  const markAllNotificationsAsRead = () => {
    setNotifications((prev) => prev.map((item) => ({ ...item, readAt: item.readAt ?? new Date().toISOString() })));
  };

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Stack direction="row" spacing={1.2} alignItems="center" sx={{ p: 2.5 }}>
        <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36 }}>
          <SportsSoccer fontSize="small" />
        </Avatar>
        <Box>
          <Typography fontWeight={700}>SportsMS</Typography>
          <Typography variant="caption" color="text.secondary">Enterprise Suite</Typography>
        </Box>
      </Stack>
      <Divider />
      <List sx={{ px: 1.5, py: 1 }}>
        <ListItemButton component={RouterLink} to="/" selected={location.pathname === '/'} sx={{ borderRadius: 2 }}>
          <ListItemIcon><Home fontSize="small" /></ListItemIcon>
          <ListItemText primary="Home" />
        </ListItemButton>
        <ListItemButton component={RouterLink} to="/posts" selected={location.pathname.startsWith('/posts')} sx={{ borderRadius: 2 }}>
          <ListItemIcon><Article fontSize="small" /></ListItemIcon>
          <ListItemText primary="Posts" />
        </ListItemButton>
        {navGroups.map((group) => {
          const visibleItems = group.items.filter((item) => item.visible);
          if (!visibleItems.length) {
            return null;
          }
          return (
            <Box key={group.label}>
              <ListItemButton onClick={() => toggleGroup(group.label)} sx={{ borderRadius: 2, mt: 0.5 }}>
                <ListItemIcon>{group.icon}</ListItemIcon>
                <ListItemText primary={group.label} />
                {openGroups[group.label] ? <ExpandLess fontSize="small" /> : <ExpandMore fontSize="small" />}
              </ListItemButton>
              <Collapse in={!!openGroups[group.label]} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  {visibleItems.map((item) => (
                    <ListItemButton
                      key={item.path}
                      component={RouterLink}
                      to={item.path}
                      selected={location.pathname === item.path}
                      sx={{ borderRadius: 2, ml: 1.5 }}
                      onClick={() => setMobileOpen(false)}
                    >
                      <ListItemIcon>{item.icon}</ListItemIcon>
                      <ListItemText primary={item.label} />
                    </ListItemButton>
                  ))}
                </List>
              </Collapse>
            </Box>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="fixed" elevation={0}>
        <Toolbar sx={{ minHeight: 72 }}>
          <IconButton sx={{ mr: 1, display: { md: 'none' } }} onClick={() => setMobileOpen(true)}>
            <MenuIcon />
          </IconButton>
          <Typography variant="subtitle1" fontWeight={700} sx={{ flexGrow: 1 }}>
            Sports Management Platform
          </Typography>
          {isAuthenticated ? (
            <>
              <Tooltip title="Notifications">
                <IconButton onClick={(event) => setNotificationAnchor(event.currentTarget)}>
                  <Badge color="error" badgeContent={unreadCount} max={99}>
                    <Notifications />
                  </Badge>
                </IconButton>
              </Tooltip>
              <IconButton onClick={(event) => setProfileAnchor(event.currentTarget)}>
                <Avatar sx={{ width: 34, height: 34 }}>{(user?.fullName || 'User').charAt(0).toUpperCase()}</Avatar>
              </IconButton>
            </>
          ) : (
            <Stack direction="row" spacing={1}>
              <Button color="inherit" component={RouterLink} to="/login">Login</Button>
              <Button color="inherit" component={RouterLink} to="/register">Register</Button>
            </Stack>
          )}
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: drawerWidth } }}
        >
          {drawerContent}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{ display: { xs: 'none', md: 'block' }, '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' } }}
        >
          {drawerContent}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, pt: 11, pb: 3, px: { xs: 1.5, md: 3 } }}>
        <Container maxWidth={false}>{children}</Container>
      </Box>

      <Menu anchorEl={notificationAnchor} open={Boolean(notificationAnchor)} onClose={() => setNotificationAnchor(null)} PaperProps={{ sx: { width: 340, maxHeight: 380 } }}>
        <MenuItem disableRipple sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography fontWeight={700}>Notifications</Typography>
          <Chip label={`${unreadCount} unread`} size="small" color={unreadCount ? 'primary' : 'default'} />
        </MenuItem>
        <Divider />
        {notifications.map((item) => (
          <MenuItem key={item.id} sx={{ whiteSpace: 'normal', alignItems: 'flex-start' }}>
            <Box>
              <Typography variant="subtitle2">{item.title}</Typography>
              <Typography variant="body2">{item.message}</Typography>
              <Typography variant="caption" color="text.secondary">{item.readAt ? 'Read' : 'Unread'}</Typography>
            </Box>
          </MenuItem>
        ))}
        {!notifications.length && <MenuItem>No notifications</MenuItem>}
        <Divider />
        <MenuItem onClick={markAllNotificationsAsRead}>Mark all as read</MenuItem>
      </Menu>

      <Menu anchorEl={profileAnchor} open={Boolean(profileAnchor)} onClose={() => setProfileAnchor(null)}>
        <MenuItem disabled>{user?.fullName || 'User'}</MenuItem>
        <MenuItem onClick={() => { setProfileAnchor(null); navigate('/dashboard'); }}>Dashboard</MenuItem>
        <MenuItem onClick={handleLogout}>Logout</MenuItem>
      </Menu>
    </Box>
  );
};

export default AppLayout;
