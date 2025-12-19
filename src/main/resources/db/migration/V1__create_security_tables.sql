-- V1: Spring Security JDBC Authentication Tables
-- These are the standard tables required by JdbcUserDetailsManager

-- Users table (standard Spring Security schema)
CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Authorities table (standard Spring Security schema)
CREATE TABLE authorities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    CONSTRAINT uk_authorities UNIQUE (username, authority)
);

-- Index for faster lookups
CREATE INDEX idx_authorities_username ON authorities(username);
