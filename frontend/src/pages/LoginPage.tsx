import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Alert,
    CircularProgress,
    InputAdornment,
    IconButton,
} from '@mui/material';
import { Visibility, VisibilityOff, School } from '@mui/icons-material';
import { useAuth } from '../auth';
import { AxiosError } from 'axios';
import type { ErrorResponse } from '../types';

export const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
    const [isLoading, setIsLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const from = (location.state as any)?.from?.pathname || '/';

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setValidationErrors({});
        setIsLoading(true);

        try {
            await login(username, password);
            navigate(from, { replace: true });
        } catch (err) {
            const axiosError = err as AxiosError<ErrorResponse>;
            if (axiosError.response?.data) {
                setError(axiosError.response.data.message || 'Autentificare eșuată');
                if (axiosError.response.data.validationErrors) {
                    setValidationErrors(axiosError.response.data.validationErrors);
                }
            } else {
                setError('Eroare de conexiune');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                p: 2,
            }}
        >
            <Card
                sx={{
                    maxWidth: 400,
                    width: '100%',
                    borderRadius: 3,
                    boxShadow: '0 8px 40px rgba(0,0,0,0.12)',
                }}
            >
                <CardContent sx={{ p: 4 }}>
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        <Box
                            sx={{
                                width: 64,
                                height: 64,
                                borderRadius: '50%',
                                bgcolor: 'primary.main',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                mx: 'auto',
                                mb: 2,
                            }}
                        >
                            <School sx={{ fontSize: 32, color: 'white' }} />
                        </Box>
                        <Typography variant="h5" fontWeight="bold" gutterBottom>
                            Admin Zone
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Student Management System
                        </Typography>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            error={!!validationErrors.username}
                            helperText={validationErrors.username}
                            margin="normal"
                            autoComplete="username"
                            autoFocus
                        />
                        <TextField
                            fullWidth
                            label="Parolă"
                            type={showPassword ? 'text' : 'password'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            error={!!validationErrors.password}
                            helperText={validationErrors.password}
                            margin="normal"
                            autoComplete="current-password"
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton onClick={() => setShowPassword(!showPassword)} edge="end">
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            size="large"
                            disabled={isLoading || !username || !password}
                            sx={{ mt: 3, mb: 2, py: 1.5, borderRadius: 2 }}
                        >
                            {isLoading ? <CircularProgress size={24} color="inherit" /> : 'Autentificare'}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </Box>
    );
};
