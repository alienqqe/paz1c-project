DROP DATABASE IF EXISTS gym_db;
CREATE DATABASE IF NOT EXISTS gym_db;
USE gym_db;

CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    last_discount_threshold_used INT NOT NULL DEFAULT 0
);

CREATE TABLE coaches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL
);

CREATE TABLE memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    startDate DATE NOT NULL,
    expiresAt DATE NOT NULL,
    price DOUBLE NOT NULL,
    type ENUM('Monthly', 'Yearly', 'Weekly', 'Ten') NOT NULL,
    discount_percent_applied INT NOT NULL DEFAULT 0,
    discount_threshold_used INT NULL,
    visits_remaining INT NULL,
    idOfHolder BIGINT  NOT NULL,
    FOREIGN KEY (idOfHolder) REFERENCES clients(id)
        ON DELETE CASCADE
);

CREATE TABLE training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    startDate DATETIME NOT NULL,
    endDate DATETIME NOT NULL,
    title VARCHAR(200) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (coach_id) REFERENCES coaches(id)
);

CREATE TABLE specializations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE coach_specializations (
    coach_id BIGINT NOT NULL,
    specialization_id BIGINT NOT NULL,
    PRIMARY KEY (coach_id, specialization_id),
    FOREIGN KEY (coach_id) REFERENCES coaches(id) ON DELETE CASCADE,
    FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE
);

CREATE TABLE visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    membership_id BIGINT,
    check_in DATETIME NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (membership_id) REFERENCES memberships(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF', 'COACH') NOT NULL DEFAULT 'STAFF',
    coach_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coach_id) REFERENCES coaches(id) ON DELETE SET NULL
);

CREATE TABLE coach_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coach_id BIGINT NOT NULL,
    startDate DATETIME NOT NULL,
    endDate DATETIME NOT NULL,
    note VARCHAR(200),
    FOREIGN KEY (coach_id) REFERENCES coaches(id) ON DELETE CASCADE
);

CREATE TABLE discount_rules(
     id INT AUTO_INCREMENT PRIMARY KEY,
     visits_threshold INT NOT NULL UNIQUE,
     discount_percent INT NOT NULL
    );
