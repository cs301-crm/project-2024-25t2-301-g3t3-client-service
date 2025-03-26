-- Drop and recreate tables
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS logs CASCADE;

-- Create clients table
CREATE TABLE clients (
    client_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'NON_BINARY', 'PREFER_NOT_TO_SAY')),
    email_address VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(15) NOT NULL,
    address VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    nric VARCHAR(9) NOT NULL UNIQUE,
    agent_id VARCHAR(36) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED')),
    PRIMARY KEY (client_id)
);

-- Create accounts table
CREATE TABLE accounts (
    account_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING', 'BUSINESS')),
    account_status VARCHAR(20) NOT NULL CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'CLOSED')),
    opening_date DATE NOT NULL,
    initial_deposit DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    branch_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (account_id),
    FOREIGN KEY (client_id) REFERENCES clients(client_id)
);

-- Create logs table
CREATE TABLE logs (
    id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(255),
    client_id VARCHAR(255),
    crud_type VARCHAR(255) NOT NULL CHECK (crud_type IN ('CREATE', 'READ', 'UPDATE', 'DELETE')),
    attribute_name VARCHAR(255),
    before_value TEXT,
    after_value TEXT,
    date_time TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

-- Insert Clients
INSERT INTO clients (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id, verification_status)
VALUES 
('c1000000-0000-0000-0000-000000000001', 'John', 'Doe', '1985-05-15', 'MALE', 'john.doe@example.com', '+6591234567', '123 Main Street', 'Singapore', 'Central', 'Singapore', '123456', 'S1234567A', 'a1000000-0000-0000-0000-000000000001', 'VERIFIED'),
('c2000000-0000-0000-0000-000000000002', 'Jane', 'Smith', '1990-08-22', 'FEMALE', 'jane.smith@example.com', '+6598765432', '456 Park Avenue', 'Singapore', 'East', 'Singapore', '654321', 'S2345678B', 'a1000000-0000-0000-0000-000000000001', 'VERIFIED'),
('c3000000-0000-0000-0000-000000000003', 'Michael', 'Wong', '1978-12-10', 'MALE', 'michael.wong@example.com', '+6590001111', '789 Orchard Road', 'Singapore', 'West', 'Singapore', '789012', 'S3456789C', 'a2000000-0000-0000-0000-000000000002', 'PENDING');

-- Insert Accounts for John Doe
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'SAVINGS', 'ACTIVE', '2020-01-15', 5000.00, 'SGD', 'BR001'),
('a2000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'CHECKING', 'ACTIVE', '2020-02-20', 3000.00, 'SGD', 'BR001'),
('a3000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000001', 'BUSINESS', 'INACTIVE', '2021-05-10', 10000.00, 'USD', 'BR002');

-- Insert Accounts for Jane Smith
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a4000000-0000-0000-0000-000000000004', 'c2000000-0000-0000-0000-000000000002', 'SAVINGS', 'ACTIVE', '2019-11-05', 8000.00, 'SGD', 'BR003'),
('a5000000-0000-0000-0000-000000000005', 'c2000000-0000-0000-0000-000000000002', 'CHECKING', 'PENDING', '2022-03-15', 2500.00, 'USD', 'BR001');

-- Insert Accounts for Michael Wong
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a6000000-0000-0000-0000-000000000006', 'c3000000-0000-0000-0000-000000000003', 'BUSINESS', 'ACTIVE', '2018-07-22', 50000.00, 'SGD', 'BR002'),
('a7000000-0000-0000-0000-000000000007', 'c3000000-0000-0000-0000-000000000003', 'SAVINGS', 'CLOSED', '2015-09-30', 15000.00, 'USD', 'BR003'),
('a8000000-0000-0000-0000-000000000008', 'c3000000-0000-0000-0000-000000000003', 'CHECKING', 'ACTIVE', '2020-12-01', 7500.00, 'SGD', 'BR001');

-- Insert sample log entries
INSERT INTO logs (id, agent_id, client_id, crud_type, attribute_name, before_value, after_value, date_time)
VALUES
-- CREATE log for John Doe
('l1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'CREATE', 'c1000000-0000-0000-0000-000000000001', '', '', '2023-01-15 10:30:00'),

-- READ log for Jane Smith
('l2000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'c2000000-0000-0000-0000-000000000002', 'READ', 'c2000000-0000-0000-0000-000000000002', '', '', '2023-02-20 14:45:00'),

-- UPDATE log for Michael Wong with multiple attribute changes
('l3000000-0000-0000-0000-000000000003', 'a2000000-0000-0000-0000-000000000002', 'c3000000-0000-0000-0000-000000000003', 'UPDATE', 'firstName|lastName|address|phoneNumber|city', 'Michael|Wong|789 Orchard Road|+6590001111|Singapore', 'Michael James|Wong|101 New Road|+6590009999|Jurong', '2023-03-10 09:15:00'),

-- DELETE log for an account
('l4000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'DELETE', 'c1000000-0000-0000-0000-000000000001', '', '', '2023-04-05 16:20:00');
