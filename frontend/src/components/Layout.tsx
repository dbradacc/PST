import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
    Box,
    Drawer,
    AppBar,
    Toolbar,
    List,
    Typography,
    Divider,
    IconButton,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Avatar,
    Menu,
    MenuItem,
    Chip,
    useTheme,
    useMediaQuery,
} from '@mui/material';
import {
    Menu as MenuIcon,
    Dashboard as DashboardIcon,
    People as PeopleIcon,
    School as SchoolIcon,
    Assignment as AssignmentIcon,
    EventNote as EventNoteIcon,
    AdminPanelSettings as AdminIcon,
    History as HistoryIcon,
    Logout as LogoutIcon,
    ChevronLeft as ChevronLeftIcon,
} from '@mui/icons-material';
import { useAuth, RoleGuard } from '../auth';

const drawerWidth = 260;

interface NavItem {
    label: string;
    path: string;
    icon: React.ReactNode;
    roles?: ('ROLE_ADMIN' | 'ROLE_SECRETARY' | 'ROLE_PROFESSOR')[];
}

const navItems: NavItem[] = [
    { label: 'Dashboard', path: '/', icon: <DashboardIcon /> },
    { label: 'Studenți', path: '/students', icon: <PeopleIcon /> },
    { label: 'Cursuri', path: '/courses', icon: <SchoolIcon /> },
    { label: 'Înscrieri', path: '/enrollments', icon: <AssignmentIcon /> },
    { label: 'Prezențe', path: '/attendance', icon: <EventNoteIcon /> },
    { label: 'Utilizatori', path: '/users', icon: <AdminIcon />, roles: ['ROLE_ADMIN'] },
    { label: 'Audit Log', path: '/audit', icon: <HistoryIcon />, roles: ['ROLE_ADMIN'] },
];

export const Layout: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [mobileOpen, setMobileOpen] = useState(false);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    const handleMenuClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleLogout = async () => {
        handleMenuClose();
        await logout();
        navigate('/login');
    };

    const handleNavClick = (path: string) => {
        navigate(path);
        if (isMobile) {
            setMobileOpen(false);
        }
    };

    const getRoleColor = (role: string): 'error' | 'warning' | 'info' => {
        if (role.includes('ADMIN')) return 'error';
        if (role.includes('SECRETARY')) return 'warning';
        return 'info';
    };

    const getRoleLabel = (role: string): string => {
        if (role.includes('ADMIN')) return 'Admin';
        if (role.includes('SECRETARY')) return 'Secretar';
        return 'Profesor';
    };

    const drawer = (
        <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Toolbar sx={{ px: 2, justifyContent: 'space-between' }}>
                <Typography variant="h6" noWrap component="div" fontWeight="bold" color="primary">
                    Admin Zone
                </Typography>
                {isMobile && (
                    <IconButton onClick={handleDrawerToggle}>
                        <ChevronLeftIcon />
                    </IconButton>
                )}
            </Toolbar>
            <Divider />
            <List sx={{ flexGrow: 1, px: 1 }}>
                {navItems.map((item) => {
                    const isActive = location.pathname === item.path;
                    const content = (
                        <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                            <ListItemButton
                                onClick={() => handleNavClick(item.path)}
                                sx={{
                                    borderRadius: 2,
                                    bgcolor: isActive ? 'primary.main' : 'transparent',
                                    color: isActive ? 'white' : 'inherit',
                                    '&:hover': {
                                        bgcolor: isActive ? 'primary.dark' : 'action.hover',
                                    },
                                }}
                            >
                                <ListItemIcon sx={{ color: isActive ? 'white' : 'inherit', minWidth: 40 }}>
                                    {item.icon}
                                </ListItemIcon>
                                <ListItemText primary={item.label} />
                            </ListItemButton>
                        </ListItem>
                    );

                    if (item.roles) {
                        return (
                            <RoleGuard key={item.path} roles={item.roles}>
                                {content}
                            </RoleGuard>
                        );
                    }
                    return content;
                })}
            </List>
            <Divider />
            <Box sx={{ p: 2 }}>
                <Typography variant="caption" color="text.secondary">
                    v1.0.1 © 2026 Admin Zone
                </Typography>
            </Box>
        </Box>
    );

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh' }}>
            <AppBar
                position="fixed"
                sx={{
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                    bgcolor: 'background.paper',
                    color: 'text.primary',
                    boxShadow: 1,
                }}
            >
                <Toolbar>
                    <IconButton
                        color="inherit"
                        edge="start"
                        onClick={handleDrawerToggle}
                        sx={{ mr: 2, display: { md: 'none' } }}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Box sx={{ flexGrow: 1 }} />
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        {user?.roles.map((role) => (
                            <Chip
                                key={role}
                                label={getRoleLabel(role)}
                                color={getRoleColor(role)}
                                size="small"
                                variant="outlined"
                            />
                        ))}
                        <IconButton onClick={handleMenuClick}>
                            <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main' }}>
                                {user?.username.charAt(0).toUpperCase()}
                            </Avatar>
                        </IconButton>
                    </Box>
                    <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
                        <MenuItem disabled>
                            <Typography variant="body2">Bine ai venit, {user?.username}!</Typography>
                        </MenuItem>
                        <Divider />
                        <MenuItem onClick={handleLogout}>
                            <ListItemIcon>
                                <LogoutIcon fontSize="small" />
                            </ListItemIcon>
                            Deconectare
                        </MenuItem>
                    </Menu>
                </Toolbar>
            </AppBar>
            <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
                <Drawer
                    variant={isMobile ? 'temporary' : 'permanent'}
                    open={isMobile ? mobileOpen : true}
                    onClose={handleDrawerToggle}
                    ModalProps={{ keepMounted: true }}
                    sx={{
                        '& .MuiDrawer-paper': {
                            boxSizing: 'border-box',
                            width: drawerWidth,
                            borderRight: '1px solid',
                            borderColor: 'divider',
                        },
                    }}
                >
                    {drawer}
                </Drawer>
            </Box>
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    bgcolor: 'grey.50',
                    minHeight: '100vh',
                }}
            >
                <Toolbar />
                <Outlet />
            </Box>
        </Box>
    );
};
