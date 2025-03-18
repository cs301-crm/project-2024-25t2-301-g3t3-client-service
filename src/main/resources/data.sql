CREATE TABLE IF NOT EXISTS user_entity (
    id UUID PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS client (
    client_id UUID PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    address VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    nric CHAR(9) UNIQUE NOT NULL,
    agent_id UUID NULL,
    FOREIGN KEY (agent_id) REFERENCES user_entity(id) ON DELETE SET NULL
);

-- Create the account table with a foreign key reference to client
CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) NOT NULL,
    opening_date DATE NOT NULL,
    initial_deposit DECIMAL(18,2) CHECK (initial_deposit >= 0) NOT NULL,
    currency CHAR(3) NOT NULL,
    branch_id VARCHAR(50) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY,
    token TEXT NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE
);

-- Clear existing data (if any)
DELETE FROM account;
DELETE FROM client;

INSERT INTO user_entity (id, first_name, last_name, username, email, password, user_role, enabled)
VALUES 
('d10e49e5-77c6-49f0-a802-96bfd1f40577', 'Alice', 'Tan', 'alice.tan', 'alice.tan@example.com', 'alicetan', 'AGENT', TRUE),
('e90822ab-fabc-4b7c-8a58-31f718d6db4e', 'Bob', 'Lee', 'bob.lee', 'bob.lee@example.com', 'boblee', 'AGENT', TRUE),
('1ecccc64-4a5b-4029-8fe9-f76f538a666a', 'Charlie', 'Ng', 'charlie.ng', 'charlie.ng@example.com', 'charlieng', 'AGENT', TRUE);

-- Insert Clients
INSERT INTO client (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id)
VALUES 
('7fb18f90-49dd-4516-a81a-92f83d1bc55e', 'John', 'Doe', '1985-05-15', 'MALE', 'john.doe@example.com', '+6591234567', '123 Main Street', 'Singapore', 'Central', 'Singapore', '123456', 'S1234567A', 'd10e49e5-77c6-49f0-a802-96bfd1f40577'),
('2459a982-0f83-4bbf-b84e-488b3da90b15', 'Jane', 'Smith', '1990-08-22', 'FEMALE', 'jane.smith@example.com', '+6598765432', '456 Park Avenue', 'Singapore', 'East', 'Singapore', '654321', 'S2345678B', 'e90822ab-fabc-4b7c-8a58-31f718d6db4e'),
('b238b5f0-7836-4cd3-852a-28451371bd9f', 'Michael', 'Wong', '1978-12-10', 'MALE', 'michael.wong@example.com', '+6590001111', '789 Orchard Road', 'Singapore', 'West', 'Singapore', '789012', 'S3456789C', '1ecccc64-4a5b-4029-8fe9-f76f538a666a');

-- Insert Accounts for John Doe
INSERT INTO account (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a7bfd6a2-7509-4520-8437-0dcb81720fd6', '7fb18f90-49dd-4516-a81a-92f83d1bc55e', 'SAVINGS', 'ACTIVE', '2020-01-15', 5000.00, 'SGD', 'BR001'),
('6a71884b-a037-4893-bc95-cd483b373bdb', '7fb18f90-49dd-4516-a81a-92f83d1bc55e', 'CHECKING', 'ACTIVE', '2020-02-20', 3000.00, 'SGD', 'BR001'),
('ee4459a3-b324-499a-a4d1-763182d32f42', '7fb18f90-49dd-4516-a81a-92f83d1bc55e', 'BUSINESS', 'INACTIVE', '2021-05-10', 10000.00, 'USD', 'BR002');

-- Insert Accounts for Jane Smith
INSERT INTO account (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('7acceeef-ae83-4310-a4ff-bbbcce1fd209', '2459a982-0f83-4bbf-b84e-488b3da90b15', 'SAVINGS', 'ACTIVE', '2019-11-05', 8000.00, 'SGD', 'BR003'),
('c9b914aa-2c0d-4cd3-94c8-ac284066c2d0', '2459a982-0f83-4bbf-b84e-488b3da90b15', 'CHECKING', 'PENDING', '2022-03-15', 2500.00, 'USD', 'BR001');

-- Insert Accounts for Michael Wong
INSERT INTO account (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('fe7e18e6-c2a3-4fbd-8add-075a31ec847e', 'b238b5f0-7836-4cd3-852a-28451371bd9f', 'BUSINESS', 'ACTIVE', '2018-07-22', 50000.00, 'SGD', 'BR002'),
('75739bd3-c240-4845-bff2-e830ef243264', 'b238b5f0-7836-4cd3-852a-28451371bd9f', 'SAVINGS', 'CLOSED', '2015-09-30', 15000.00, 'USD', 'BR003'),
('65307ebf-0367-483e-9c5a-66a1e1a5ebad', 'b238b5f0-7836-4cd3-852a-28451371bd9f', 'CHECKING', 'ACTIVE', '2020-12-01', 7500.00, 'SGD', 'BR001');

-- Insert Refresh Tokens for the users
INSERT INTO refresh_token (id, token, user_id, created_at, expires_at)
VALUES 
('c05a7e14-86cd-4e9e-8c4d-4c2c9a5e3f76', '$2a$10$l/ABcYzjGiqEJlYGtijAGufqNpUjd1Ws92Hhx6uUPakAjJ8jMfdHK', 'd10e49e5-77c6-49f0-a802-96bfd1f40577', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days'),
('b72ff29a-6e88-42f8-92ad-34b8f5937fcd', '$2a$10$l/ABcYzjGiqEJlYGtijAGufqNpUjd1Ws92Hhx6uUPakAjJ8jMfdHK', 'e90822ab-fabc-4b7c-8a58-31f718d6db4e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days'),
('d96a4e88-2c9d-4f7f-b5d4-52d8a3a7c9e1', '$2a$10$l/ABcYzjGiqEJlYGtijAGufqNpUjd1Ws92Hhx6uUPakAjJ8jMfdHK', '1ecccc64-4a5b-4029-8fe9-f76f538a666a', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days');