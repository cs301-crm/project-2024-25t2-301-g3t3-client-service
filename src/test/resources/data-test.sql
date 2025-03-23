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

-- Insert Test Clients
INSERT INTO clients (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id, verification_status)
VALUES 
('c1000000-0000-0000-0000-000000000001', 'Test', 'User', '1990-01-01', 'MALE', 'test.user@example.com', '+6599999999', '1 Test Street', 'Test City', 'Test State', 'Test Country', '123456', 'S9876543Z', 'test-agent001', 'VERIFIED'),
('c2000000-0000-0000-0000-000000000002', 'Test', 'Admin', '1985-05-05', 'FEMALE', 'test.admin@example.com', '+6588888888', '2 Test Avenue', 'Test City', 'Test State', 'Test Country', '654321', 'S8765432Y', 'test-agent002', 'PENDING');

-- Insert Test Accounts
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'SAVINGS', 'ACTIVE', '2020-01-01', 1000.00, 'SGD', 'BR001'),
('a2000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'CHECKING', 'ACTIVE', '2020-01-01', 2000.00, 'SGD', 'BR001'),
('a3000000-0000-0000-0000-000000000003', 'c2000000-0000-0000-0000-000000000002', 'BUSINESS', 'ACTIVE', '2020-01-01', 5000.00, 'USD', 'BR002');

-- Insert Test Log Entries
INSERT INTO logs (id, agent_id, client_id, crud_type, attribute_name, before_value, after_value, date_time)
VALUES
-- CREATE log for Test User (client profile)
('l1000000-0000-0000-0000-000000000001', 'test-agent001', 'c1000000-0000-0000-0000-000000000001', 'CREATE', 'c1000000-0000-0000-0000-000000000001', '', '', '2023-01-01 10:00:00'),

-- READ log for Test Admin (client profile)
('l2000000-0000-0000-0000-000000000002', 'test-agent002', 'c2000000-0000-0000-0000-000000000002', 'READ', 'c2000000-0000-0000-0000-000000000002', '', '', '2023-01-02 11:00:00'),

-- UPDATE log for Test User (client profile attributes)
('l3000000-0000-0000-0000-000000000003', 'test-agent001', 'c1000000-0000-0000-0000-000000000001', 'UPDATE', 'address|phoneNumber', '1 Test Street|+6599999999', '3 New Street|+6577777777', '2023-01-03 12:00:00'),

-- DELETE log for an account (account-related)
('l4000000-0000-0000-0000-000000000004', 'test-agent002', 'c2000000-0000-0000-0000-000000000002', 'DELETE', 'c2000000-0000-0000-0000-000000000002', '', '', '2023-01-04 13:00:00'),

-- CREATE log for a savings account (account-related)
('l5000000-0000-0000-0000-000000000005', 'test-agent001', 'c1000000-0000-0000-0000-000000000001', 'CREATE', 'a1000000-0000-0000-0000-000000000001', '', '', '2023-01-05 14:00:00'),

-- UPDATE log for verification status (client profile)
('l6000000-0000-0000-0000-000000000006', 'test-agent002', 'c2000000-0000-0000-0000-000000000002', 'UPDATE', 'verificationStatus', 'PENDING', 'VERIFIED', '2023-01-06 15:00:00'),

-- READ log for an account (account-related)
('l7000000-0000-0000-0000-000000000007', 'test-agent001', 'c1000000-0000-0000-0000-000000000001', 'READ', 'a2000000-0000-0000-0000-000000000002', '', '', '2023-01-07 16:00:00'),

-- UPDATE log for account status (account-related)
('l8000000-0000-0000-0000-000000000008', 'test-agent002', 'c2000000-0000-0000-0000-000000000002', 'UPDATE', 'accountStatus', 'ACTIVE', 'INACTIVE', '2023-01-08 17:00:00');
