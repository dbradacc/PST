import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box, Paper, Typography, Button, TextField, Dialog, DialogTitle, DialogContent,
    DialogActions, IconButton, Chip, MenuItem, Autocomplete, Alert,
} from '@mui/material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useSnackbar } from 'notistack';
import { attendanceApi, studentsApi, coursesApi } from '../api/client';
import { useAuth } from '../auth';
import type { Attendance, AttendanceRequest, Student, Course } from '../types';

export const AttendancePage: React.FC = () => {
    const { canCreate, canDelete } = useAuth();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [selectedAttendance, setSelectedAttendance] = useState<Attendance | null>(null);
    const [businessError, setBusinessError] = useState<string | null>(null);

    const { control, handleSubmit, reset } = useForm<AttendanceRequest>({
        defaultValues: { studentId: 0, courseId: 0, data: '', semester: 1, status: 'prezent' },
    });

    const { data, isLoading } = useQuery({
        queryKey: ['attendance', paginationModel.page, paginationModel.pageSize],
        queryFn: () => attendanceApi.getAll({ page: paginationModel.page, size: paginationModel.pageSize }),
    });

    const { data: studentsData } = useQuery({
        queryKey: ['students-all'],
        queryFn: () => studentsApi.getAll({ size: 1000 }),
    });

    const { data: coursesData } = useQuery({
        queryKey: ['courses-all'],
        queryFn: () => coursesApi.getAll({ size: 1000 }),
    });

    const createMutation = useMutation({
        mutationFn: attendanceApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['attendance'] });
            enqueueSnackbar('Prezență adăugată', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            const message = error.response?.data?.message || 'Eroare';
            // Check for business rule violation (max 14)
            if (error.response?.status === 422) {
                setBusinessError(message);
            } else {
                enqueueSnackbar(message, { variant: 'error' });
            }
        },
    });

    const deleteMutation = useMutation({
        mutationFn: attendanceApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['attendance'] });
            enqueueSnackbar('Prezență ștearsă', { variant: 'success' });
            setDeleteDialogOpen(false);
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const handleOpenCreate = () => {
        setBusinessError(null);
        reset({ studentId: 0, courseId: 0, data: new Date().toISOString().split('T')[0], semester: 1, status: 'prezent' });
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setBusinessError(null);
        reset();
    };

    const onSubmit = (formData: AttendanceRequest) => {
        setBusinessError(null);
        createMutation.mutate(formData);
    };

    const handleDelete = (attendance: Attendance) => {
        setSelectedAttendance(attendance);
        setDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedAttendance) {
            deleteMutation.mutate(selectedAttendance.id);
        }
    };

    const getStatusColor = (status: string): 'success' | 'error' | 'warning' => {
        if (status === 'prezent') return 'success';
        if (status === 'absent') return 'error';
        return 'warning';
    };

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'ID', width: 70 },
        { field: 'studentName', headerName: 'Student', flex: 1 },
        { field: 'courseName', headerName: 'Curs', flex: 1.2 },
        { field: 'data', headerName: 'Data', width: 110 },
        {
            field: 'semester',
            headerName: 'Sem',
            width: 70,
            renderCell: (params) => <Chip label={params.value} size="small" variant="outlined" />,
        },
        {
            field: 'status',
            headerName: 'Status',
            width: 100,
            renderCell: (params) => (
                <Chip label={params.value} size="small" color={getStatusColor(params.value)} />
            ),
        },
        {
            field: 'actions',
            headerName: 'Acțiuni',
            width: 80,
            sortable: false,
            renderCell: (params) => (
                <Box>
                    {canDelete && (
                        <IconButton size="small" color="error" onClick={() => handleDelete(params.row)}>
                            <DeleteIcon fontSize="small" />
                        </IconButton>
                    )}
                </Box>
            ),
        },
    ];

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5" fontWeight="bold">Prezențe</Typography>
                {canCreate && (
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
                        Adaugă Prezență
                    </Button>
                )}
            </Box>

            <Paper sx={{ height: 500 }}>
                <DataGrid
                    rows={data?.data || []}
                    columns={columns}
                    rowCount={data?.totalElements || 0}
                    loading={isLoading}
                    paginationMode="server"
                    paginationModel={paginationModel}
                    onPaginationModelChange={setPaginationModel}
                    pageSizeOptions={[10, 25, 50]}
                    disableRowSelectionOnClick
                />
            </Paper>

            {/* Create Dialog */}
            <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogTitle>Adaugă Prezență</DialogTitle>
                    <DialogContent>
                        {businessError && (
                            <Alert severity="warning" sx={{ mb: 2 }}>
                                {businessError}
                            </Alert>
                        )}
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Controller
                                name="studentId"
                                control={control}
                                render={({ field }) => (
                                    <Autocomplete
                                        options={studentsData?.data || []}
                                        getOptionLabel={(option: Student) => `${option.nume} ${option.prenume}`}
                                        onChange={(_, value) => field.onChange(value?.id || 0)}
                                        renderInput={(params) => <TextField {...params} label="Student" required />}
                                    />
                                )}
                            />
                            <Controller
                                name="courseId"
                                control={control}
                                render={({ field }) => (
                                    <Autocomplete
                                        options={coursesData?.data || []}
                                        getOptionLabel={(option: Course) => option.denumire}
                                        onChange={(_, value) => field.onChange(value?.id || 0)}
                                        renderInput={(params) => <TextField {...params} label="Curs" required />}
                                    />
                                )}
                            />
                            <Controller
                                name="data"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} type="date" label="Data" InputLabelProps={{ shrink: true }} required />
                                )}
                            />
                            <Controller
                                name="semester"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} select label="Semestru" required
                                        onChange={(e) => field.onChange(Number(e.target.value))}>
                                        <MenuItem value={1}>Semestrul 1</MenuItem>
                                        <MenuItem value={2}>Semestrul 2</MenuItem>
                                    </TextField>
                                )}
                            />
                            <Controller
                                name="status"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} select label="Status" required>
                                        <MenuItem value="prezent">Prezent</MenuItem>
                                        <MenuItem value="absent">Absent</MenuItem>
                                        <MenuItem value="motivat">Motivat</MenuItem>
                                    </TextField>
                                )}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Anulează</Button>
                        <Button type="submit" variant="contained" disabled={createMutation.isPending}>
                            Creează
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>

            {/* Delete Confirmation */}
            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Confirmă ștergerea</DialogTitle>
                <DialogContent>
                    <Typography>
                        Sigur doriți să ștergeți prezența din {selectedAttendance?.data}?
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteDialogOpen(false)}>Anulează</Button>
                    <Button color="error" variant="contained" onClick={confirmDelete} disabled={deleteMutation.isPending}>
                        Șterge
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};
