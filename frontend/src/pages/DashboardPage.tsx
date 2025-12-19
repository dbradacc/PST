import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box,
    Grid,
    Card,
    CardContent,
    Typography,
    Skeleton,
    Button,
    Paper,
} from '@mui/material';
import {
    People as PeopleIcon,
    School as SchoolIcon,
    Assignment as AssignmentIcon,
    EventNote as EventNoteIcon,
    Download as DownloadIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth';
import { studentsApi, coursesApi, enrollmentsApi, attendanceApi, exportApi } from '../api/client';
import { useSnackbar } from 'notistack';

interface StatCardProps {
    title: string;
    value: number | undefined;
    icon: React.ReactNode;
    color: string;
    isLoading: boolean;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color, isLoading }) => (
    <Card sx={{ height: '100%', position: 'relative', overflow: 'visible' }}>
        <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                        {title}
                    </Typography>
                    {isLoading ? (
                        <Skeleton width={60} height={40} />
                    ) : (
                        <Typography variant="h4" fontWeight="bold">
                            {value ?? 0}
                        </Typography>
                    )}
                </Box>
                <Box
                    sx={{
                        width: 56,
                        height: 56,
                        borderRadius: 2,
                        bgcolor: color,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white',
                    }}
                >
                    {icon}
                </Box>
            </Box>
        </CardContent>
    </Card>
);

export const DashboardPage: React.FC = () => {
    const { user } = useAuth();
    const { enqueueSnackbar } = useSnackbar();

    const { data: studentsData, isLoading: studentsLoading } = useQuery({
        queryKey: ['students-count'],
        queryFn: () => studentsApi.getAll({ size: 1 }),
    });

    const { data: coursesData, isLoading: coursesLoading } = useQuery({
        queryKey: ['courses-count'],
        queryFn: () => coursesApi.getAll({ size: 1 }),
    });

    const { data: enrollmentsData, isLoading: enrollmentsLoading } = useQuery({
        queryKey: ['enrollments-count'],
        queryFn: () => enrollmentsApi.getAll({ size: 1 }),
    });

    const { data: attendanceData, isLoading: attendanceLoading } = useQuery({
        queryKey: ['attendance-count'],
        queryFn: () => attendanceApi.getAll({ size: 1 }),
    });

    const handleExport = async (type: 'students' | 'courses' | 'attendance' | 'enrollments') => {
        try {
            await exportApi.downloadCsv(type);
            enqueueSnackbar(`Export ${type} descărcat cu succes`, { variant: 'success' });
        } catch {
            enqueueSnackbar(`Eroare la export ${type}`, { variant: 'error' });
        }
    };

    const stats = [
        {
            title: 'Studenți',
            value: studentsData?.totalElements,
            icon: <PeopleIcon />,
            color: '#3f51b5',
            isLoading: studentsLoading,
        },
        {
            title: 'Cursuri',
            value: coursesData?.totalElements,
            icon: <SchoolIcon />,
            color: '#f50057',
            isLoading: coursesLoading,
        },
        {
            title: 'Înscrieri',
            value: enrollmentsData?.totalElements,
            icon: <AssignmentIcon />,
            color: '#ff9800',
            isLoading: enrollmentsLoading,
        },
        {
            title: 'Prezențe',
            value: attendanceData?.totalElements,
            icon: <EventNoteIcon />,
            color: '#4caf50',
            isLoading: attendanceLoading,
        },
    ];

    return (
        <Box>
            <Box sx={{ mb: 4 }}>
                <Typography variant="h4" fontWeight="bold" gutterBottom>
                    Bine ai venit, {user?.username}! 👋
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Aceasta este pagina principală a sistemului de management studenți.
                </Typography>
            </Box>

            <Grid container spacing={3} sx={{ mb: 4 }}>
                {stats.map((stat) => (
                    <Grid item xs={12} sm={6} md={3} key={stat.title}>
                        <StatCard {...stat} />
                    </Grid>
                ))}
            </Grid>

            <Paper sx={{ p: 3 }}>
                <Typography variant="h6" fontWeight="bold" gutterBottom>
                    Export Rapoarte CSV
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Descarcă date în format CSV pentru rapoarte și analize.
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
                    <Button
                        variant="outlined"
                        startIcon={<DownloadIcon />}
                        onClick={() => handleExport('students')}
                    >
                        Export Studenți
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<DownloadIcon />}
                        onClick={() => handleExport('courses')}
                    >
                        Export Cursuri
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<DownloadIcon />}
                        onClick={() => handleExport('enrollments')}
                    >
                        Export Înscrieri
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<DownloadIcon />}
                        onClick={() => handleExport('attendance')}
                    >
                        Export Prezențe
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
};
