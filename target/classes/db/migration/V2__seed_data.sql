-- QueueLess Seed Data
-- Version: V2__seed_data.sql

INSERT INTO organizations (id, name, plan_tier, status) VALUES
(1, 'City Care Hospital', 'ENTERPRISE', 'ACTIVE'),
(2, 'Metro National Bank', 'PRO', 'ACTIVE');

INSERT INTO branches (id, organization_id, name, address, latitude, longitude, operating_hours_json) VALUES
(1, 1, 'City Care Main Campus - Downtown', '100 Medical Center Blvd, Cityville', 37.7749, -122.4194, '{"open":"08:00","close":"20:00"}'),
(2, 2, 'Metro Bank - Financial District', '45 Wall Street, Metro City', 40.7128, -74.0060, '{"open":"09:00","close":"17:00"}');

INSERT INTO services (id, branch_id, organization_id, name, avg_duration_min, daily_capacity, grace_period_min, requires_identity_verification) VALUES
(1, 1, 1, 'Emergency Triage & OPD', 8, 300, 10, TRUE),
(2, 1, 1, 'General Cardiology OPD', 15, 100, 15, FALSE),
(3, 2, 2, 'Cash & Deposit Counters', 5, 500, 10, FALSE),
(4, 2, 2, 'Loan & Wealth Management Desk', 20, 50, 15, TRUE);

INSERT INTO counters (id, branch_id, organization_id, name, status) VALUES
(1, 1, 1, 'Counter 1 (Emergency)', 'ONLINE'),
(2, 1, 1, 'Counter 2 (General)', 'ONLINE'),
(3, 2, 2, 'Teller Window 1', 'ONLINE'),
(4, 2, 2, 'Teller Window 2', 'ONLINE');

INSERT INTO counter_service_mappings (counter_id, service_id) VALUES
(1, 1),
(2, 1),
(2, 2),
(3, 3),
(4, 3),
(4, 4);

-- Users (Pass: password123 -> BCrypt: $2a$10$eACCqjNl4dY1B4rT6A4E/e1R.t/P8uWz7wWp/Xk/2uX6QJzS4a3yS)
INSERT INTO users (id, name, phone, email, password_hash, device_fingerprint, status) VALUES
(1, 'Super Admin User', '+1000000000', 'superadmin@queueless.com', '$2a$10$eACCqjNl4dY1B4rT6A4E/e1R.t/P8uWz7wWp/Xk/2uX6QJzS4a3yS', 'fp-super-admin', 'ACTIVE'),
(2, 'Hospital Admin', '+1999888777', 'admin@cityhospital.com', '$2a$10$eACCqjNl4dY1B4rT6A4E/e1R.t/P8uWz7wWp/Xk/2uX6QJzS4a3yS', 'fp-hosp-admin', 'ACTIVE'),
(3, 'Dr. Sarah Smith (Operator)', '+1555444333', 'operator@cityhospital.com', '$2a$10$KDmWsbODf8lRtE8MRdsmu.krGlvaWjTtdvHjD8nE3o/pjLAMeETYu', 'fp-operator-1', 'ACTIVE'),
(4, 'John Customer', '+1111222333', 'john.doe@gmail.com', '$2a$10$eACCqjNl4dY1B4rT6A4E/e1R.t/P8uWz7wWp/Xk/2uX6QJzS4a3yS', 'fp-cust-1', 'ACTIVE'),
(5, 'Jane Senior Citizen', '+1111222444', 'jane.senior@gmail.com', '$2a$10$eACCqjNl4dY1B4rT6A4E/e1R.t/P8uWz7wWp/Xk/2uX6QJzS4a3yS', 'fp-cust-2', 'ACTIVE');

INSERT INTO user_roles (id, user_id, organization_id, role) VALUES
(1, 1, 1, 'SUPER_ADMIN'),
(2, 2, 1, 'ORG_ADMIN'),
(3, 3, 1, 'OPERATOR'),
(4, 4, 1, 'USER'),
(5, 5, 1, 'USER');
