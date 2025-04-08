-- Note: The agent_id values reference user IDs from the User Service

-- Test clients with verification_status explicitly set
INSERT INTO clients (client_id, first_name, last_name, date_of_birth, gender, email_address, phone_number, address, city, state, country, postal_code, nric, agent_id, verification_status)
VALUES
('c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'John', 'Doe', '1990-01-01', 'MALE', 'john.doe@example.com', '1234567890', '123 Main St', 'Singapore', 'Singapore', 'Singapore', '123456', 'S1234567A', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'VERIFIED'),
('c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e', 'Jane', 'Smith', '1992-02-02', 'FEMALE', 'jane.smith@example.com', '0987654321', '456 Oak St', 'Singapore', 'Singapore', 'Singapore', '654321', 'S7654321B', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'VERIFIED'),
('c3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2f', 'Bob', 'Johnson', '1985-03-03', 'MALE', 'bob.johnson@example.com', '5555555555', '789 Pine St', 'Singapore', 'Singapore', 'Singapore', '555555', 'S5555555C', '0818fcb0-ee20-4f67-88b0-a68c21b6be1e', 'PENDING');

-- Test logs
INSERT INTO logs (id, client_id, agent_id, crud_type, attribute_name, before_value, after_value, date_time)
VALUES
('l1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2i', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', '', '{"clientId":"c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d","firstName":"John","lastName":"Doe"}', '2023-01-01 10:00:00'),
('l2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2j', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'UPDATE', 'phone_number', '1234567890', '9876543210', '2023-01-02 11:00:00'),
('l3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2k', 'c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e', '', '{"clientId":"c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e","firstName":"Jane","lastName":"Smith"}', '2023-01-03 12:00:00'),
('l4a5b6c7-d8e9-4a5b-9c8d-7e6f5a4b3c2l', 'c3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2f', '0818fcb0-ee20-4f67-88b0-a68c21b6be1e', 'CREATE', 'c3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2f', '', '{"clientId":"c3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2f","firstName":"Bob","lastName":"Johnson"}', '2023-01-04 13:00:00'),
('l5a6b7c8-d9e0-4a5b-9c8d-7e6f5a4b3c2m', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2i', '', '{"accountId":"a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2i","clientId":"c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d","accountType":"SAVINGS"}', '2023-01-05 14:00:00');

-- Test accounts - Added after clients to ensure foreign key constraints are satisfied
INSERT INTO accounts (account_id, client_id, account_type, account_status, opening_date, initial_deposit, currency, branch_id)
VALUES
('a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2i', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'SAVINGS', 'ACTIVE', '2023-01-01', 1000.00, 'SGD', 'BR001'),
('a2b3c4d5-e6f7-4a5b-9c8d-7e6f5a4b3c2j', 'c1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'CHECKING', 'ACTIVE', '2023-01-02', 2000.00, 'SGD', 'BR001'),
('a3b4c5d6-e7f8-4a5b-9c8d-7e6f5a4b3c2k', 'c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e', 'SAVINGS', 'ACTIVE', '2023-01-03', 3000.00, 'SGD', 'BR002'),
('a4b5c6d7-e8f9-4a5b-9c8d-7e6f5a4b3c2l', 'c2a3b4c5-d6e7-4a5b-9c8d-7e6f5a4b3c2e', 'FIXED_DEPOSIT', 'ACTIVE', '2023-01-04', 4000.00, 'SGD', 'BR002'),
('a5b6c7d8-e9f0-4a5b-9c8d-7e6f5a4b3c2m', 'c3a4b5c6-d7e8-4a5b-9c8d-7e6f5a4b3c2f', 'SAVINGS', 'PENDING', '2023-01-05', 5000.00, 'SGD', 'BR003');
