import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box,
    Paper,
    Typography,
    Button,
    TextField,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    IconButton,
    Chip,
    MenuItem,
} from '@mui/material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSnackbar } from 'notistack';
import { coursesApi } from '../api/client';
import { useAuth } from '../auth';
import type { Course, CourseRequest } from '../types';

const courseSchema = z.object({
    denumire: z.string().min(3, 'Minim 3 caractere').max(255),
    profesorTitular: z.string().min(3, 'Minim 3 caractere').max(255),
    nrCredite: z.number().min(1).max(30),
    semester: z.number().min(1).max(2),
});

export const CoursesPage: React.FC = () => {
    const { canCreate, canDelete } = useAuth();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
    const [semester, setSemester] = useState<number | ''>('');
    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);

    const { control, handleSubmit, reset, formState: { errors } } = useForm<CourseRequest>({
        resolver: zodResolver(courseSchema),
        defaultValues: { denumire: '', profesorTitular: '', nrCredite: 5, semester: 1 },
    });

    const { data, isLoading } = useQuery({
        queryKey: ['courses', paginationModel.page, paginationModel.pageSize, semester],
        queryFn: () => coursesApi.getAll({
            page: paginationModel.page,
            size: paginationModel.pageSize,
            semester: semester || undefined,
        }),
    });

    const createMutation = useMutation({
        mutationFn: coursesApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['courses'] });
            enqueueSnackbar('Curs creat cu succes', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: CourseRequest }) => coursesApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['courses'] });
            enqueueSnackbar('Curs actualizat', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: coursesApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['courses'] });
            enqueueSnackbar('Curs șters', { variant: 'success' });
            setDeleteDialogOpen(false);
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const handleOpenCreate = () => {
        setSelectedCourse(null);
        reset({ denumire: '', profesorTitular: '', nrCredite: 5, semester: 1 });
        setDialogOpen(true);
    };

    const handleOpenEdit = (course: Course) => {
        setSelectedCourse(course);
        reset({
            denumire: course.denumire,
            profesorTitular: course.profesorTitular,
            nrCredite: course.nrCredite,
            semester: course.semester,
        });
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setSelectedCourse(null);
        reset();
    };

    const onSubmit = (formData: CourseRequest) => {
        if (selectedCourse) {
            updateMutation.mutate({ id: selectedCourse.id, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    const handleDelete = (course: Course) => {
        setSelectedCourse(course);
        setDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedCourse) {
            deleteMutation.mutate(selectedCourse.id);
        }
    };

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'ID', width: 70 },
        { field: 'denumire', headerName: 'Denumire', flex: 1.5 },
        { field: 'profesorTitular', headerName: 'Profesor Titular', flex: 1 },
        {
            field: 'nrCredite',
            headerName: 'Credite',
            width: 90,
            renderCell: (params) => <Chip label={params.value} size="small" color="primary" />,
        },
        {
            field: 'semester',
            headerName: 'Semestru',
            width: 100,
            renderCell: (params) => <Chip label={`Sem ${params.value}`} size="small" variant="outlined" />,
        },
        {
            field: 'actions',
            headerName: 'Acțiuni',
            width: 120,
            sortable: false,
            renderCell: (params) => (
                <Box>
                    {canCreate && (
                        <IconButton size="small" onClick={() => handleOpenEdit(params.row)}>
                            <EditIcon fontSize="small" />
                        </IconButton>
                    )}
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
                <Typography variant="h5" fontWeight="bold">Cursuri</Typography>
                {canCreate && (
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
                        Adaugă Curs
                    </Button>
                )}
            </Box>

            <Paper sx={{ p: 2, mb: 2 }}>
                <TextField
                    size="small"
                    select
                    label="Semestru"
                    value={semester}
                    onChange={(e) => setSemester(e.target.value === '' ? '' : Number(e.target.value))}
                    sx={{ minWidth: 140 }}
                >
                    <MenuItem value="">Toate</MenuItem>
                    <MenuItem value={1}>Semestrul 1</MenuItem>
                    <MenuItem value={2}>Semestrul 2</MenuItem>
                </TextField>
            </Paper>

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

            {/* Create/Edit Dialog */}
            <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogTitle>{selectedCourse ? 'Editează Curs' : 'Adaugă Curs'}</DialogTitle>
                    <DialogContent>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Controller name="denumire" control={control} render={({ field }) => (
                                <TextField {...field} label="Denumire" error={!!errors.denumire} helperText={errors.denumire?.message} fullWidth />
                            )} />
                            <Controller name="profesorTitular" control={control} render={({ field }) => (
                                <TextField {...field} label="Profesor Titular" error={!!errors.profesorTitular} helperText={errors.profesorTitular?.message} fullWidth />
                            )} />
                            <Controller name="nrCredite" control={control} render={({ field }) => (
                                <TextField {...field} type="number" label="Nr. Credite" error={!!errors.nrCredite} helperText={errors.nrCredite?.message} fullWidth
                                    onChange={(e) => field.onChange(Number(e.target.value))} />
                            )} />
                            <Controller name="semester" control={control} render={({ field }) => (
                                <TextField {...field} select label="Semestru" error={!!errors.semester} helperText={errors.semester?.message} fullWidth
                                    onChange={(e) => field.onChange(Number(e.target.value))}>
                                    <MenuItem value={1}>Semestrul 1</MenuItem>
                                    <MenuItem value={2}>Semestrul 2</MenuItem>
                                </TextField>
                            )} />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Anulează</Button>
                        <Button type="submit" variant="contained" disabled={createMutation.isPending || updateMutation.isPending}>
                            {selectedCourse ? 'Salvează' : 'Creează'}
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>

            {/* Delete Confirmation */}
            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Confirmă ștergerea</DialogTitle>
                <DialogContent>
                    <Typography>Sigur doriți să ștergeți cursul {selectedCourse?.denumire}?</Typography>
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
