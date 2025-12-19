import axios, { AxiosError } from 'axios';
import type { ErrorResponse } from '../types';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error: AxiosError<ErrorResponse>) => {
        if (error.response?.status === 401) {
            // Redirect to login if unauthorized (except for login/me endpoints)
            const isAuthEndpoint = error.config?.url?.includes('/auth/');
            if (!isAuthEndpoint) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;

// Auth API
export const authApi = {
    login: async (username: string, password: string) => {
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        const response = await api.post('/auth/login', formData, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });
        return response.data;
    },

    logout: async () => {
        const response = await api.post('/auth/logout');
        return response.data;
    },

    me: async () => {
        const response = await api.get('/auth/me');
        return response.data;
    },
};

// Students API
export const studentsApi = {
    getAll: async (params?: { q?: string; anStudiu?: number; page?: number; size?: number }) => {
        const response = await api.get('/students', { params });
        return response.data;
    },

    getById: async (id: number) => {
        const response = await api.get(`/students/${id}`);
        return response.data;
    },

    create: async (data: any) => {
        const response = await api.post('/students', data);
        return response.data;
    },

    update: async (id: number, data: any) => {
        const response = await api.put(`/students/${id}`, data);
        return response.data;
    },

    delete: async (id: number) => {
        await api.delete(`/students/${id}`);
    },
};

// Courses API
export const coursesApi = {
    getAll: async (params?: { q?: string; semester?: number; page?: number; size?: number }) => {
        const response = await api.get('/courses', { params });
        return response.data;
    },

    getById: async (id: number) => {
        const response = await api.get(`/courses/${id}`);
        return response.data;
    },

    create: async (data: any) => {
        const response = await api.post('/courses', data);
        return response.data;
    },

    update: async (id: number, data: any) => {
        const response = await api.put(`/courses/${id}`, data);
        return response.data;
    },

    delete: async (id: number) => {
        await api.delete(`/courses/${id}`);
    },
};

// Enrollments API
export const enrollmentsApi = {
    getAll: async (params?: { page?: number; size?: number }) => {
        const response = await api.get('/enrollments', { params });
        return response.data;
    },

    create: async (data: any) => {
        const response = await api.post('/enrollments', data);
        return response.data;
    },

    update: async (studentId: number, courseId: number, data: any) => {
        const response = await api.put(`/enrollments/${studentId}/${courseId}`, data);
        return response.data;
    },

    delete: async (studentId: number, courseId: number) => {
        await api.delete(`/enrollments/${studentId}/${courseId}`);
    },
};

// Attendance API
export const attendanceApi = {
    getAll: async (params?: { page?: number; size?: number }) => {
        const response = await api.get('/attendance', { params });
        return response.data;
    },

    search: async (params?: { student?: string; course?: string; semester?: number }) => {
        const response = await api.get('/attendance/search', { params });
        return response.data;
    },

    getStats: async () => {
        const response = await api.get('/attendance/stats');
        return response.data;
    },

    create: async (data: any) => {
        const response = await api.post('/attendance', data);
        return response.data;
    },

    update: async (id: number, data: any) => {
        const response = await api.put(`/attendance/${id}`, data);
        return response.data;
    },

    delete: async (id: number) => {
        await api.delete(`/attendance/${id}`);
    },
};

// Users API
export const usersApi = {
    getAll: async () => {
        const response = await api.get('/users');
        return response.data;
    },

    create: async (data: any) => {
        const response = await api.post('/users', data);
        return response.data;
    },

    update: async (username: string, data: any) => {
        const response = await api.put(`/users/${username}`, data);
        return response.data;
    },

    delete: async (username: string) => {
        await api.delete(`/users/${username}`);
    },
};

// Audit API
export const auditApi = {
    getAll: async (params?: { page?: number; size?: number }) => {
        const response = await api.get('/audit', { params });
        return response.data;
    },

    getByUsername: async (username: string, params?: { page?: number; size?: number }) => {
        const response = await api.get(`/audit/user/${username}`, { params });
        return response.data;
    },
};

// Export API
export const exportApi = {
    downloadCsv: async (type: 'students' | 'courses' | 'attendance' | 'enrollments') => {
        const response = await api.get(`/export/csv/${type}`, {
            responseType: 'blob',
        });

        const filename = `${type}.csv`;
        const blob = new Blob([response.data], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    },
};
