import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box, Paper, Typography, Button, TextField, Dialog, DialogTitle, DialogContent,
    DialogActions, IconButton, Chip, FormControlLabel, Switch, Autocomplete,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useSnackbar } from 'notistack';
import { usersApi } from '../api/client';
import type { User, UserRequest } from '../types';

const roleOptions = ['ROLE_ADMIN', 'ROLE_SECRETARY', 'ROLE_PROFESSOR'];

export const UsersPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [isEdit, setIsEdit] = useState(false);

    const { control, handleSubmit, reset } = useForm<UserRequest>({
        defaultValues: { username: '', password: '', enabled: true, roles: ['ROLE_PROFESSOR'] },
    });

    const { data, isLoading } = useQuery({
        queryKey: ['users'],
        queryFn: usersApi.getAll,
    });

    const createMutation = useMutation({
        mutationFn: usersApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['users'] });
            enqueueSnackbar('Utilizator creat', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ username, data }: { username: string; data: UserRequest }) =>
            usersApi.update(username, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['users'] });
            enqueueSnackbar('Utilizator actualizat', { variant: 'success' });
            handleCloseDialog();
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: usersApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['users'] });
            enqueueSnackbar('Utilizator șters', { variant: 'success' });
            setDeleteDialogOpen(false);
        },
        onError: (error: any) => {
            enqueueSnackbar(error.response?.data?.message || 'Eroare', { variant: 'error' });
        },
    });

    const handleOpenCreate = () => {
        setSelectedUser(null);
        setIsEdit(false);
        reset({ username: '', password: '', enabled: true, roles: ['ROLE_PROFESSOR'] });
        setDialogOpen(true);
    };

    const handleOpenEdit = (user: User) => {
        setSelectedUser(user);
        setIsEdit(true);
        reset({ username: user.username, password: '', enabled: user.enabled, roles: user.roles });
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setSelectedUser(null);
        reset();
    };

    const onSubmit = (formData: UserRequest) => {
        if (isEdit && selectedUser) {
            updateMutation.mutate({ username: selectedUser.username, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    const handleDelete = (user: User) => {
        setSelectedUser(user);
        setDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedUser) {
            deleteMutation.mutate(selectedUser.username);
        }
    };

    const getRoleColor = (role: string): 'error' | 'warning' | 'info' => {
        if (role.includes('ADMIN')) return 'error';
        if (role.includes('SECRETARY')) return 'warning';
        return 'info';
    };

    const columns: GridColDef[] = [
        { field: 'username', headerName: 'Username', flex: 1 },
        {
            field: 'roles',
            headerName: 'Roluri',
            flex: 1.5,
            renderCell: (params) => (
                <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                    {(params.value || []).map((role: string) => (
                        <Chip key={role} label={role.replace('ROLE_', '')} size="small" color={getRoleColor(role)} />
                    ))}
                </Box>
            ),
        },
        {
            field: 'enabled',
            headerName: 'Activ',
            width: 80,
            renderCell: (params) => (
                <Chip label={params.value ? 'Da' : 'Nu'} size="small" color={params.value ? 'success' : 'default'} />
            ),
        },
        {
            field: 'actions',
            headerName: 'Acțiuni',
            width: 120,
            sortable: false,
            renderCell: (params) => (
                <Box>
                    <IconButton size="small" onClick={() => handleOpenEdit(params.row)}>
                        <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => handleDelete(params.row)}>
                        <DeleteIcon fontSize="small" />
                    </IconButton>
                </Box>
            ),
        },
    ];

    const rows = (data || []).map((u: User) => ({ ...u, id: u.username }));

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5" fontWeight="bold">Utilizatori</Typography>
                <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
                    Adaugă Utilizator
                </Button>
            </Box>

            <Paper sx={{ height: 500 }}>
                <DataGrid
                    rows={rows}
                    columns={columns}
                    loading={isLoading}
                    pageSizeOptions={[10, 25]}
                    disableRowSelectionOnClick
                />
            </Paper>

            {/* Create/Edit Dialog */}
            <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogTitle>{isEdit ? 'Editează Utilizator' : 'Adaugă Utilizator'}</DialogTitle>
                    <DialogContent>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Controller
                                name="username"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} label="Username" disabled={isEdit} required fullWidth />
                                )}
                            />
                            <Controller
                                name="password"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        type="password"
                                        label={isEdit ? 'Parolă nouă (opțional)' : 'Parolă'}
                                        required={!isEdit}
                                        fullWidth
                                    />
                                )}
                            />
                            <Controller
                                name="roles"
                                control={control}
                                render={({ field }) => (
                                    <Autocomplete
                                        multiple
                                        options={roleOptions}
                                        value={field.value}
                                        onChange={(_, value) => field.onChange(value)}
                                        renderInput={(params) => <TextField {...params} label="Roluri" />}
                                        renderTags={(value, getTagProps) =>
                                            value.map((option, index) => (
                                                <Chip {...getTagProps({ index })} label={option.replace('ROLE_', '')} size="small" />
                                            ))
                                        }
                                    />
                                )}
                            />
                            <Controller
                                name="enabled"
                                control={control}
                                render={({ field }) => (
                                    <FormControlLabel
                                        control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                                        label="Cont activ"
                                    />
                                )}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Anulează</Button>
                        <Button type="submit" variant="contained" disabled={createMutation.isPending || updateMutation.isPending}>
                            {isEdit ? 'Salvează' : 'Creează'}
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>

            {/* Delete Confirmation */}
            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Confirmă ștergerea</DialogTitle>
                <DialogContent>
                    <Typography>Sigur doriți să ștergeți utilizatorul {selectedUser?.username}?</Typography>
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
