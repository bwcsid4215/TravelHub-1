-- Insert sample travel requests
INSERT INTO travel_requests (travel_request_id, employee_id, project_id, start_date, end_date, purpose, manager_present, status, created_at, updated_at) 
VALUES 
('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2024-02-01', '2024-02-05', 'Client meeting in New York', true, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440004', '2024-02-10', '2024-02-15', 'Tech conference in San Francisco', false, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample expenses
INSERT INTO travel_expenses (expense_id, expense_date, travel_request_id, created_at)
VALUES 
('660e8400-e29b-41d4-a716-446655440000', '2024-02-02', '550e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440001', '2024-02-03', '550e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP);

-- Insert sample expense items
INSERT INTO expense_items (item_id, category, amount, description, expense_id, created_at)
VALUES 
('770e8400-e29b-41d4-a716-446655440000', 'Meals', 75.50, 'Dinner with client', '660e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP),
('770e8400-e29b-41d4-a716-446655440001', 'Transportation', 45.00, 'Taxi fare', '660e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP),
('770e8400-e29b-41d4-a716-446655440002', 'Hotel', 200.00, 'Hotel stay', '660e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP);

-- Insert sample bookings
INSERT INTO travel_bookings (booking_id, booking_type, details, notes, travel_request_id, created_at)
VALUES 
('880e8400-e29b-41d4-a716-446655440000', 'FLIGHT', 'Flight BA123 - JFK to LHR', 'Business class', '550e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP),
('880e8400-e29b-41d4-a716-446655440001', 'HOTEL', 'Hilton Hotel - Room 501', 'Executive suite', '550e8400-e29b-41d4-a716-446655440000', CURRENT_TIMESTAMP);

-- Insert sample attachments
INSERT INTO travel_attachments (attachment_id, file_name, file_type, file_url, file_size, travel_request_id, uploaded_at)
VALUES 
('990e8400-e29b-41d4-a716-446655440000', 'conference_invitation.pdf', 'application/pdf', 'https://example.com/files/invitation.pdf', 2048576, '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP);