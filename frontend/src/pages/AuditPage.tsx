import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Box, Paper, Typography, TextField, Chip, InputAdornment } from '@mui/material';
import { DataGrid, GridColDef, GridPaginationModel } from '@mui/x-data-grid';
import { Search as SearchIcon } from '@mui/icons-material';
import { auditApi } from '../api/client';


export const AuditPage: React.FC = () => {
    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 25 });
    const [usernameFilter, setUsernameFilter] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['audit', paginationModel.page, paginationModel.pageSize, usernameFilter],
        queryFn: () =>
            usernameFilter
                ? auditApi.getByUsername(usernameFilter, { page: paginationModel.page, size: paginationModel.pageSize })
                : auditApi.getAll({ page: paginationModel.page, size: paginationModel.pageSize }),
    });

    const getActionColor = (action: string): 'success' | 'warning' | 'error' | 'info' | 'default' => {
        if (action.includes('CREATE') || action.includes('LOGIN_SUCCESS')) return 'success';
        if (action.includes('UPDATE')) return 'warning';
        if (action.includes('DELETE')) return 'error';
        if (action.includes('FAILED') || action.includes('DENIED')) return 'error';
        return 'info';
    };

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'ID', width: 70 },
        {
            field: 'timestamp',
            headerName: 'Data/Ora',
            width: 170,
            renderCell: (params) => new Date(params.value).toLocaleString('ro-RO'),
        },
        { field: 'username', headerName: 'Utilizator', width: 120 },
        {
            field: 'action',
            headerName: 'Acțiune',
            width: 150,
            renderCell: (params) => <Chip label={params.value} size="small" color={getActionColor(params.value)} />,
        },
        { field: 'entity', headerName: 'Entitate', width: 120 },
        { field: 'entityId', headerName: 'ID Entitate', width: 90 },
        { field: 'ip', headerName: 'IP', width: 130 },
        {
            field: 'payloadJson',
            headerName: 'Detalii',
            flex: 1,
            renderCell: (params) => (
                <Typography variant="caption" sx={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {params.value ? params.value.substring(0, 100) + (params.value.length > 100 ? '...' : '') : '-'}
                </Typography>
            ),
        },
    ];

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5" fontWeight="bold">Audit Log</Typography>
            </Box>

            <Paper sx={{ p: 2, mb: 2 }}>
                <TextField
                    size="small"
                    placeholder="Filtrează după utilizator..."
                    value={usernameFilter}
                    onChange={(e) => setUsernameFilter(e.target.value)}
                    InputProps={{
                        startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
                    }}
                    sx={{ minWidth: 250 }}
                />
            </Paper>

            <Paper sx={{ height: 600 }}>
                <DataGrid
                    rows={data?.data || []}
                    columns={columns}
                    rowCount={data?.totalElements || 0}
                    loading={isLoading}
                    paginationMode="server"
                    paginationModel={paginationModel}
                    onPaginationModelChange={setPaginationModel}
                    pageSizeOptions={[25, 50, 100]}
                    disableRowSelectionOnClick
                />
            </Paper>
        </Box>
    );
};
