import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { CircularProgress, Box } from '@mui/material';
import { useAuth } from './AuthContext';
import type { Role } from '../types';

interface RequireAuthProps {
    children: React.ReactNode;
    roles?: Role[];
}

export const RequireAuth: React.FC<RequireAuthProps> = ({ children, roles }) => {
    const { isAuthenticated, isLoading, hasAnyRole } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (roles && roles.length > 0 && !hasAnyRole(roles)) {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};

interface RoleGuardProps {
    children: React.ReactNode;
    roles: Role[];
    fallback?: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ children, roles, fallback = null }) => {
    const { hasAnyRole } = useAuth();

    if (!hasAnyRole(roles)) {
        return <>{fallback}</>;
    }

    return <>{children}</>;
};
