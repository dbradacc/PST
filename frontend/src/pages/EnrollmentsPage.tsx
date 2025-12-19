import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box, Paper, Typography, Button, TextField, Dialog, DialogTitle, DialogContent,
    DialogActions, IconButton, Chip, Autocomplete,
} from '@mui/material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useSnackbar } from 'notistack';
import { enrollmentsApi, studentsApi, coursesApi } from '../api/client';
import { useAuth } from '../auth';
import type { Enrollment, EnrollmentRequest, Student, Course } from '../types';

export const EnrollmentsPage: React.FC = () => {
    const { canCreate, canDelete } = useAuth();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [selectedEnrollment, setSelectedEnrollment] = useState<Enrollment | null>(null);

    const { control, handleSubmit, reset } = useForm<EnrollmentRequest>({
        defaultValues: { studentId: 0, courseId: 0, notaFinala: undefined },
    });

    const { data, isLoading } = useQuery({
        queryKey: ['enrollments', paginationModel.page, paginationModel.pageSize],
        queryFn: () => enrollmentsApi.getAll({ page: paginationModel.page, size: paginationModel.pageSize }),
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
        mutationFn: enrollmentsApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['enrollments'] });
            enqueueSnackbar('Înscriere creată', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare - posibil duplicat', { variant: 'error' });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: ({ studentId, courseId }: { studentId: number; courseId: number }) =>
            enrollmentsApi.delete(studentId, courseId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['enrollments'] });
            enqueueSnackbar('Înscriere ștearsă', { variant: 'success' });
            setDeleteDialogOpen(false);
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const handleOpenCreate = () => {
        reset({ studentId: 0, courseId: 0, notaFinala: undefined });
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        reset();
    };

    const onSubmit = (formData: EnrollmentRequest) => {
        createMutation.mutate(formData);
    };

    const handleDelete = (enrollment: Enrollment) => {
        setSelectedEnrollment(enrollment);
        setDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedEnrollment) {
            deleteMutation.mutate({
                studentId: selectedEnrollment.studentId,
                courseId: selectedEnrollment.courseId,
            });
        }
    };

    const columns: GridColDef[] = [
        { field: 'studentName', headerName: 'Student', flex: 1 },
        { field: 'courseName', headerName: 'Curs', flex: 1.5 },
        {
            field: 'notaFinala',
            headerName: 'Notă',
            width: 100,
            renderCell: (params) =>
                params.value ? (
                    <Chip label={params.value} color={params.value >= 5 ? 'success' : 'error'} size="small" />
                ) : (
                    <Chip label="-" variant="outlined" size="small" />
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

    // Create unique row IDs from composite key
    const rows = (data?.data || []).map((e: Enrollment) => ({
        ...e,
        id: `${e.studentId}-${e.courseId}`,
    }));

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5" fontWeight="bold">Înscrieri</Typography>
                {canCreate && (
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
                        Adaugă Înscriere
                    </Button>
                )}
            </Box>

            <Paper sx={{ height: 500 }}>
                <DataGrid
                    rows={rows}
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
                    <DialogTitle>Adaugă Înscriere</DialogTitle>
                    <DialogContent>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Controller
                                name="studentId"
                                control={control}
                                render={({ field }) => (
                                    <Autocomplete
                                        options={studentsData?.data || []}
                                        getOptionLabel={(option: Student) => `${option.nume} ${option.prenume} (${option.email})`}
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
                                        getOptionLabel={(option: Course) => `${option.denumire} (Sem ${option.semester})`}
                                        onChange={(_, value) => field.onChange(value?.id || 0)}
                                        renderInput={(params) => <TextField {...params} label="Curs" required />}
                                    />
                                )}
                            />
                            <Controller
                                name="notaFinala"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        type="number"
                                        label="Notă Finală (opțional)"
                                        inputProps={{ min: 1, max: 10, step: 0.01 }}
                                        onChange={(e) => field.onChange(e.target.value ? Number(e.target.value) : undefined)}
                                    />
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
                        Sigur doriți să ștergeți înscrierea {selectedEnrollment?.studentName} la{' '}
                        {selectedEnrollment?.courseName}?
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
