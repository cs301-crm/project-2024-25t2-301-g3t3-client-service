-- Clear existing data (if any)
DELETE FROM accounts;
DELETE FROM clients;

-- Insert Clients
INSERT INTO clients (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id)
VALUES 
('c1000000-0000-0000-0000-000000000001', 'John', 'Doe', '1985-05-15', 'MALE', 'john.doe@example.com', '+6591234567', '123 Main Street', 'Singapore', 'Central', 'Singapore', '123456', 'S1234567A', 'agent001'),
('c2000000-0000-0000-0000-000000000002', 'Jane', 'Smith', '1990-08-22', 'FEMALE', 'jane.smith@example.com', '+6598765432', '456 Park Avenue', 'Singapore', 'East', 'Singapore', '654321', 'S2345678B', 'agent001'),
('c3000000-0000-0000-0000-000000000003', 'Michael', 'Wong', '1978-12-10', 'MALE', 'michael.wong@example.com', '+6590001111', '789 Orchard Road', 'Singapore', 'West', 'Singapore', '789012', 'S3456789C', 'agent002');

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
