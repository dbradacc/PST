// API Response types matching backend

export interface PageResponse<T> {
    data: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    validationErrors?: Record<string, string>;
}

export interface Student {
    id: number;
    nume: string;
    prenume: string;
    email: string;
    telefon: string | null;
    anStudiu: number;
    createdAt: string;
    updatedAt: string;
}

export interface StudentRequest {
    nume: string;
    prenume: string;
    email: string;
    telefon?: string;
    anStudiu: number;
}

export interface Course {
    id: number;
    denumire: string;
    profesorTitular: string;
    nrCredite: number;
    semester: number;
    createdAt: string;
    updatedAt: string;
}

export interface CourseRequest {
    denumire: string;
    profesorTitular: string;
    nrCredite: number;
    semester: number;
}

export interface Enrollment {
    studentId: number;
    studentName: string;
    courseId: number;
    courseName: string;
    notaFinala: number | null;
}

export interface EnrollmentRequest {
    studentId: number;
    courseId: number;
    notaFinala?: number;
}

export interface Attendance {
    id: number;
    studentId: number;
    studentName: string;
    courseId: number;
    courseName: string;
    data: string;
    semester: number;
    status: 'prezent' | 'absent' | 'motivat';
}

export interface AttendanceRequest {
    studentId: number;
    courseId: number;
    data: string;
    semester: number;
    status: 'prezent' | 'absent' | 'motivat';
}

export interface AttendanceStats {
    studentId: number;
    studentName: string;
    semester1Count: number;
    semester2Count: number;
}

export interface User {
    username: string;
    enabled: boolean;
    roles: string[];
}

export interface UserRequest {
    username: string;
    password?: string;
    enabled?: boolean;
    roles: string[];
}

export interface AuditLog {
    id: number;
    username: string;
    ip: string;
    action: string;
    entity: string;
    entityId: number | null;
    payloadJson: string | null;
    timestamp: string;
}

export interface LoginResponse {
    username: string;
    roles: string[];
    message: string;
}

export type Role = 'ROLE_ADMIN' | 'ROLE_SECRETARY' | 'ROLE_PROFESSOR';
