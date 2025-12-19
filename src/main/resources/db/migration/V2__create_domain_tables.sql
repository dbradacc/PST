-- V2: Domain Tables for Admin Zone Application

-- Students table (Studenti)
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nume VARCHAR(100) NOT NULL,
    prenume VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefon VARCHAR(20),
    an_studiu INT NOT NULL CHECK (an_studiu BETWEEN 1 AND 6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Courses table (Cursuri)
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    profesor_titular VARCHAR(255) NOT NULL,
    nr_credite INT NOT NULL CHECK (nr_credite > 0),
    semester INT NOT NULL CHECK (semester IN (1, 2)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Enrollments table (Inscrieri) - Composite Primary Key
CREATE TABLE enrollments (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    nota_finala DECIMAL(4,2) CHECK (nota_finala IS NULL OR (nota_finala >= 1 AND nota_finala <= 10)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, course_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Attendances table (Prezente)
CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    data DATE NOT NULL,
    semester INT NOT NULL CHECK (semester IN (1, 2)),
    status VARCHAR(20) NOT NULL CHECK (status IN ('prezent', 'absent', 'motivat')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendances_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendances_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Index for attendance queries (student + course + semester combination)
CREATE INDEX idx_attendances_student_course_semester ON attendances(student_id, course_id, semester);

-- Audit Log table
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    ip VARCHAR(45),
    action VARCHAR(50) NOT NULL,
    entity VARCHAR(100),
    entity_id BIGINT,
    payload_json TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE SET NULL
);

-- Index for audit log queries
CREATE INDEX idx_audit_log_username ON audit_log(username);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_entity ON audit_log(entity, entity_id);
