import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/client';
import type { Role } from '../types';

interface AuthUser {
    username: string;
    roles: Role[];
}

interface AuthContextType {
    user: AuthUser | null;
    isLoading: boolean;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    hasRole: (role: Role) => boolean;
    hasAnyRole: (roles: Role[]) => boolean;
    canCreate: boolean;
    canDelete: boolean;
    isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const checkAuth = useCallback(async () => {
        try {
            const response = await authApi.me();
            if (response.username) {
                setUser({
                    username: response.username,
                    roles: response.roles || [],
                });
            }
        } catch {
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    const login = async (username: string, password: string) => {
        const response = await authApi.login(username, password);
        setUser({
            username: response.username,
            roles: response.roles || [],
        });
    };

    const logout = async () => {
        await authApi.logout();
        setUser(null);
    };

    const hasRole = (role: Role): boolean => {
        return user?.roles.includes(role) ?? false;
    };

    const hasAnyRole = (roles: Role[]): boolean => {
        return roles.some((role) => hasRole(role));
    };

    const isAdmin = hasRole('ROLE_ADMIN');
    const canCreate = hasAnyRole(['ROLE_ADMIN', 'ROLE_SECRETARY']);
    const canDelete = isAdmin;

    const value: AuthContextType = {
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        logout,
        hasRole,
        hasAnyRole,
        canCreate,
        canDelete,
        isAdmin,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
