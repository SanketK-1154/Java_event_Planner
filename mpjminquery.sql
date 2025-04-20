CREATE DATABASE IF NOT exists event_planner;
USE event_planner;

CREATE TABLE IF NOT exists users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role ENUM('ADMIN', 'USER')
);
select * from users;

INSERT INTO users (name, email, password) VALUES ('Test User', 'test@example.com', 'password123');

CREATE TABLE IF NOT exists events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    title VARCHAR(255),
    date_time DATETIME,
    location VARCHAR(255),
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reminders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    event_id INT,
    reminder_time DATETIME NOT NULL,
    message TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);