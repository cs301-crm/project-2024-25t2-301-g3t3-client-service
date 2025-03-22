-- Clear existing data (if any)
DELETE FROM accounts;
DELETE FROM clients;

-- Insert Test Clients
INSERT INTO clients (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id)
VALUES 
('c1000000-0000-0000-0000-000000000001', 'Test', 'User', '1990-01-01', 'MALE', 'test.user@example.com', '+6599999999', '1 Test Street', 'Test City', 'Test State', 'Test Country', '123456', 'S9876543Z', 'test-agent001'),
('c2000000-0000-0000-0000-000000000002', 'Test', 'Admin', '1985-05-05', 'FEMALE', 'test.admin@example.com', '+6588888888', '2 Test Avenue', 'Test City', 'Test State', 'Test Country', '654321', 'S8765432Y', 'test-agent002');

-- Insert Test Accounts
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES 
('a1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'SAVINGS', 'ACTIVE', '2020-01-01', 1000.00, 'SGD', 'BR001'),
('a2000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'CHECKING', 'ACTIVE', '2020-01-01', 2000.00, 'SGD', 'BR001'),
('a3000000-0000-0000-0000-000000000003', 'c2000000-0000-0000-0000-000000000002', 'BUSINESS', 'ACTIVE', '2020-01-01', 5000.00, 'USD', 'BR002');
