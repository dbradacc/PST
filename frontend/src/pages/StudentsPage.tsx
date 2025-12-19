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
    InputAdornment,
    MenuItem,
} from '@mui/material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import {
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    Search as SearchIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSnackbar } from 'notistack';
import { studentsApi } from '../api/client';
import { useAuth } from '../auth';
import type { Student, StudentRequest } from '../types';

const studentSchema = z.object({
    nume: z.string().min(2, 'Minim 2 caractere').max(100),
    prenume: z.string().min(2, 'Minim 2 caractere').max(100),
    email: z.string().email('Email invalid'),
    telefon: z.string().optional(),
    anStudiu: z.number().min(1).max(6),
});

export const StudentsPage: React.FC = () => {
    const { canCreate, canDelete } = useAuth();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
    const [search, setSearch] = useState('');
    const [anStudiu, setAnStudiu] = useState<number | ''>('');
    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);

    const { control, handleSubmit, reset, formState: { errors } } = useForm<StudentRequest>({
        resolver: zodResolver(studentSchema),
        defaultValues: { nume: '', prenume: '', email: '', telefon: '', anStudiu: 1 },
    });

    const { data, isLoading } = useQuery({
        queryKey: ['students', paginationModel.page, paginationModel.pageSize, search, anStudiu],
        queryFn: () => studentsApi.getAll({
            page: paginationModel.page,
            size: paginationModel.pageSize,
            q: search || undefined,
            anStudiu: anStudiu || undefined,
        }),
    });

    const createMutation = useMutation({
        mutationFn: studentsApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['students'] });
            enqueueSnackbar('Student creat cu succes', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: StudentRequest }) => studentsApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['students'] });
            enqueueSnackbar('Student actualizat', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: studentsApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['students'] });
            enqueueSnackbar('Student șters', { variant: 'success' });
            setDeleteDialogOpen(false);
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const handleOpenCreate = () => {
        setSelectedStudent(null);
        reset({ nume: '', prenume: '', email: '', telefon: '', anStudiu: 1 });
        setDialogOpen(true);
    };

    const handleOpenEdit = (student: Student) => {
        setSelectedStudent(student);
        reset({
            nume: student.nume,
            prenume: student.prenume,
            email: student.email,
            telefon: student.telefon || '',
            anStudiu: student.anStudiu,
        });
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setSelectedStudent(null);
        reset();
    };

    const onSubmit = (formData: StudentRequest) => {
        if (selectedStudent) {
            updateMutation.mutate({ id: selectedStudent.id, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    const handleDelete = (student: Student) => {
        setSelectedStudent(student);
        setDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedStudent) {
            deleteMutation.mutate(selectedStudent.id);
        }
    };

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'ID', width: 70 },
        { field: 'nume', headerName: 'Nume', flex: 1 },
        { field: 'prenume', headerName: 'Prenume', flex: 1 },
        { field: 'email', headerName: 'Email', flex: 1.5 },
        { field: 'telefon', headerName: 'Telefon', width: 130 },
        {
            field: 'anStudiu',
            headerName: 'An',
            width: 80,
            renderCell: (params) => <Chip label={`Anul ${params.value}`} size="small" />,
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
                <Typography variant="h5" fontWeight="bold">Studenți</Typography>
                {canCreate && (
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
                        Adaugă Student
                    </Button>
                )}
            </Box>

            <Paper sx={{ p: 2, mb: 2 }}>
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                    <TextField
                        size="small"
                        placeholder="Caută..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        InputProps={{
                            startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
                        }}
                        sx={{ minWidth: 200 }}
                    />
                    <TextField
                        size="small"
                        select
                        label="An studiu"
                        value={anStudiu}
                        onChange={(e) => setAnStudiu(e.target.value === '' ? '' : Number(e.target.value))}
                        sx={{ minWidth: 120 }}
                    >
                        <MenuItem value="">Toți</MenuItem>
                        {[1, 2, 3, 4, 5, 6].map((an) => (
                            <MenuItem key={an} value={an}>Anul {an}</MenuItem>
                        ))}
                    </TextField>
                </Box>
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
                    <DialogTitle>{selectedStudent ? 'Editează Student' : 'Adaugă Student'}</DialogTitle>
                    <DialogContent>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Controller name="nume" control={control} render={({ field }) => (
                                <TextField {...field} label="Nume" error={!!errors.nume} helperText={errors.nume?.message} fullWidth />
                            )} />
                            <Controller name="prenume" control={control} render={({ field }) => (
                                <TextField {...field} label="Prenume" error={!!errors.prenume} helperText={errors.prenume?.message} fullWidth />
                            )} />
                            <Controller name="email" control={control} render={({ field }) => (
                                <TextField {...field} label="Email" type="email" error={!!errors.email} helperText={errors.email?.message} fullWidth />
                            )} />
                            <Controller name="telefon" control={control} render={({ field }) => (
                                <TextField {...field} label="Telefon" error={!!errors.telefon} helperText={errors.telefon?.message} fullWidth />
                            )} />
                            <Controller name="anStudiu" control={control} render={({ field }) => (
                                <TextField {...field} select label="An Studiu" error={!!errors.anStudiu} helperText={errors.anStudiu?.message} fullWidth
                                    onChange={(e) => field.onChange(Number(e.target.value))}>
                                    {[1, 2, 3, 4, 5, 6].map((an) => (
                                        <MenuItem key={an} value={an}>Anul {an}</MenuItem>
                                    ))}
                                </TextField>
                            )} />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Anulează</Button>
                        <Button type="submit" variant="contained" disabled={createMutation.isPending || updateMutation.isPending}>
                            {selectedStudent ? 'Salvează' : 'Creează'}
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>

            {/* Delete Confirmation */}
            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Confirmă ștergerea</DialogTitle>
                <DialogContent>
                    <Typography>
                        Sigur doriți să ștergeți studentul {selectedStudent?.nume} {selectedStudent?.prenume}?
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
