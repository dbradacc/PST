import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SnackbarProvider } from 'notistack';
import { AuthProvider, RequireAuth } from './auth';
import { Layout } from './components';
import {
    LoginPage,
    DashboardPage,
    StudentsPage,
    CoursesPage,
    EnrollmentsPage,
    AttendancePage,
    UsersPage,
    AuditPage,
} from './pages';
import { theme } from './theme';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
});

const App: React.FC = () => {
    return (
        <QueryClientProvider client={queryClient}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <SnackbarProvider
                    maxSnack={3}
                    anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                    autoHideDuration={3000}
                >
                    <AuthProvider>
                        <BrowserRouter>
                            <Routes>
                                <Route path="/login" element={<LoginPage />} />
                                <Route
                                    path="/"
                                    element={
                                        <RequireAuth>
                                            <Layout />
                                        </RequireAuth>
                                    }
                                >
                                    <Route index element={<DashboardPage />} />
                                    <Route path="students" element={<StudentsPage />} />
                                    <Route path="courses" element={<CoursesPage />} />
                                    <Route path="enrollments" element={<EnrollmentsPage />} />
                                    <Route path="attendance" element={<AttendancePage />} />
                                    <Route
                                        path="users"
                                        element={
                                            <RequireAuth roles={['ROLE_ADMIN']}>
                                                <UsersPage />
                                            </RequireAuth>
                                        }
                                    />
                                    <Route
                                        path="audit"
                                        element={
                                            <RequireAuth roles={['ROLE_ADMIN']}>
                                                <AuditPage />
                                            </RequireAuth>
                                        }
                                    />
                                </Route>
                                <Route path="*" element={<Navigate to="/" replace />} />
                            </Routes>
                        </BrowserRouter>
                    </AuthProvider>
                </SnackbarProvider>
            </ThemeProvider>
        </QueryClientProvider>
    );
};

export default App;
