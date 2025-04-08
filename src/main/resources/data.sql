-- Note: The agent_id values reference user IDs from the User Service

-- Production clients
INSERT INTO clients (
  client_id, first_name, last_name, date_of_birth, gender,
  email_address, phone_number, address, city, state,
  country, postal_code, nric, agent_id, verification_status
)
VALUES
('c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'Michael', 'Johnson', '1988-05-15', 'MALE',
 'michael.johnson@example.com', '9123456789', '789 Orchard Rd', 'Singapore', 'Singapore',
 'Singapore', '238912', 'S9876543A', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'VERIFIED'),

('d7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c', 'Sarah', 'Williams', '1992-08-21', 'FEMALE',
 'sarah.williams@example.com', '8234567890', '456 Clementi Rd', 'Singapore', 'Singapore',
 'Singapore', '129634', 'S8765432B', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'VERIFIED'),

('e6d5c4b3-a2f1-4e3d-9c8b-7a6f5e4d3c2b', 'David', 'Brown', '1985-11-30', 'MALE',
 'david.brown@example.com', '7345678901', '123 Tampines St', 'Singapore', 'Singapore',
 'Singapore', '520123', 'S7654321C', '0818fcb0-ee20-4f67-88b0-a68c21b6be1e', 'VERIFIED');

-- Production accounts
INSERT INTO accounts (
  account_id, client_id, account_type, account_status, 
  opening_date, initial_deposit, currency, branch_id
)
VALUES
('a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'SAVINGS', 'ACTIVE', '2023-02-15', 2500.00, 'SGD', 'BR001'),
('b2c3d4e5-f6a7-4b5c-9d8e-7f6f5a4b3c2e', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'CHECKING', 'ACTIVE', '2023-02-16', 3500.00, 'SGD', 'BR001'),
('c3d4e5f6-a7b8-4c5d-9e8f-7f6f5a4b3c2f', 'd7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c', 'SAVINGS', 'ACTIVE', '2023-02-17', 4500.00, 'SGD', 'BR002'),
('d4e5f6a7-b8c9-4d5e-9f8a-7f6f5a4b3c2g', 'd7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c', 'BUSINESS', 'ACTIVE', '2023-02-18', 5500.00, 'SGD', 'BR002');

-- Production logs
INSERT INTO logs (
  id, client_id, agent_id, crud_type, attribute_name,
  before_value, after_value, date_time
)
VALUES
('f1a2b3c4-d5e6-4a5b-9c8d-7e6f5a4b3c2d', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', '', '{"clientId":"c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f","firstName":"Michael","lastName":"Johnson"}', '2023-02-15 10:00:00'),
('f2b3c4d5-e6f7-4a5b-9c8d-7e6f5a4b3c2e', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'UPDATE', 'phone_number', '9123456789', '9123456780', '2023-02-16 11:00:00'),
('f3c4d5e6-f7a8-4a5b-9c8d-7e6f5a4b3c2f', 'd7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'd7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c', '', '{"clientId":"d7e6f5d4-c3b2-4a1e-9f8d-7c6b5a4e3d2c","firstName":"Sarah","lastName":"Williams"}', '2023-02-17 12:00:00'),
('f4d5e6f7-a8b9-4a5b-9c8d-7e6f5a4b3c2g', 'e6d5c4b3-a2f1-4e3d-9c8b-7a6f5e4d3c2b', '0818fcb0-ee20-4f67-88b0-a68c21b6be1e', 'CREATE', 'e6d5c4b3-a2f1-4e3d-9c8b-7a6f5e4d3c2b', '', '{"clientId":"e6d5c4b3-a2f1-4e3d-9c8b-7a6f5e4d3c2b","firstName":"David","lastName":"Brown"}', '2023-02-18 13:00:00'),
('f5e6f7a8-b9c0-4a5b-9c8d-7e6f5a4b3c2h', 'c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f', 'b494cfcf-d372-4292-8a93-e0db95105e2c', 'CREATE', 'a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d', '', '{"accountId":"a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d","clientId":"c8f2f4a1-e1b3-4d7c-a9e5-6f8b9c0d1e2f","accountType":"SAVINGS"}', '2023-02-19 14:00:00');
