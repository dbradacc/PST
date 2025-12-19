-- V3: Seed Data for Development and Testing

-- Default users with BCrypt encoded passwords
-- Password for all users: 'password123'
-- BCrypt hash generated with strength 10
INSERT INTO users (username, password, enabled) VALUES
    ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', TRUE),
    ('secretar', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', TRUE),
    ('profesor', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', TRUE);

-- Assign roles to users
INSERT INTO authorities (username, authority) VALUES
    ('admin', 'ROLE_ADMIN'),
    ('secretar', 'ROLE_SECRETARY'),
    ('profesor', 'ROLE_PROFESSOR');

-- Sample Students
INSERT INTO students (nume, prenume, email, telefon, an_studiu) VALUES
    ('Popescu', 'Ion', 'ion.popescu@student.edu', '0721123456', 1),
    ('Ionescu', 'Maria', 'maria.ionescu@student.edu', '0722234567', 2),
    ('Georgescu', 'Andrei', 'andrei.georgescu@student.edu', '0723345678', 3),
    ('Dumitrescu', 'Elena', 'elena.dumitrescu@student.edu', '0724456789', 1),
    ('Constantinescu', 'Alexandru', 'alex.const@student.edu', '0725567890', 2);

-- Sample Courses
INSERT INTO courses (denumire, profesor_titular, nr_credite, semester) VALUES
    ('Programare Web', 'Prof. Dr. Mihai Popa', 6, 1),
    ('Baze de Date', 'Prof. Dr. Ana Stoica', 5, 1),
    ('Algoritmi si Structuri de Date', 'Prof. Dr. George Enescu', 6, 2),
    ('Retele de Calculatoare', 'Prof. Dr. Cristina Marin', 5, 2),
    ('Inteligenta Artificiala', 'Prof. Dr. Adrian Negru', 6, 2);

-- Sample Enrollments
INSERT INTO enrollments (student_id, course_id, nota_finala) VALUES
    (1, 1, 8.50),
    (1, 2, 9.00),
    (2, 1, 7.50),
    (2, 3, 8.00),
    (3, 2, 9.50),
    (3, 4, 8.75),
    (4, 1, NULL),
    (5, 3, 7.00);

-- Sample Attendances
INSERT INTO attendances (student_id, course_id, data, semester, status) VALUES
    (1, 1, '2024-10-01', 1, 'prezent'),
    (1, 1, '2024-10-08', 1, 'prezent'),
    (1, 1, '2024-10-15', 1, 'absent'),
    (1, 2, '2024-10-02', 1, 'prezent'),
    (2, 1, '2024-10-01', 1, 'prezent'),
    (2, 1, '2024-10-08', 1, 'motivat'),
    (3, 2, '2024-10-02', 1, 'prezent'),
    (3, 4, '2024-03-01', 2, 'prezent');

-- Initial audit log entry
INSERT INTO audit_log (username, ip, action, entity, entity_id, payload_json) VALUES
    ('admin', '127.0.0.1', 'SYSTEM_INIT', 'System', NULL, '{"message": "System initialized with seed data"}');
