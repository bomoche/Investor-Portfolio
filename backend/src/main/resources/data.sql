-- Development seed data.
-- Runs on every startup because the H2 database is in-memory and
-- ddl-auto=create-drop rebuilds the schema each time.
--
-- All passwords are the BCrypt hash of "password123".
-- Investors are chosen so that every business rule has both a passing and a
-- failing case available without editing data by hand.

-- Thabo Mokoena, born 1955 — over 65, retirement withdrawals allowed.
INSERT INTO investors (first_name, last_name, email, password_hash, date_of_birth, created_at)
VALUES ('Thabo', 'Mokoena', 'thabo.mokoena@enviro365.co.za',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '1955-03-14', CURRENT_TIMESTAMP);

-- Naledi Dlamini, born 1992 — under 65, retirement withdrawals blocked.
INSERT INTO investors (first_name, last_name, email, password_hash, date_of_birth, created_at)
VALUES ('Naledi', 'Dlamini', 'naledi.dlamini@enviro365.co.za',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '1992-11-02', CURRENT_TIMESTAMP);

-- Sipho Khumalo, born 1978 — under 65, savings only.
INSERT INTO investors (first_name, last_name, email, password_hash, date_of_birth, created_at)
VALUES ('Sipho', 'Khumalo', 'sipho.khumalo@enviro365.co.za',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '1978-07-21', CURRENT_TIMESTAMP);

-- Thabo's products
INSERT INTO products (product_name, product_type, current_balance, investor_id, created_at)
VALUES ('Enviro365 Green Savings', 'SAVINGS', 125000.00, 1, CURRENT_TIMESTAMP);
INSERT INTO products (product_name, product_type, current_balance, investor_id, created_at)
VALUES ('Enviro365 Retirement Annuity', 'RETIREMENT', 850000.00, 1, CURRENT_TIMESTAMP);

-- Naledi's products — the retirement one exercises the age rule
INSERT INTO products (product_name, product_type, current_balance, investor_id, created_at)
VALUES ('Enviro365 Flexi Savings', 'SAVINGS', 47500.50, 2, CURRENT_TIMESTAMP);
INSERT INTO products (product_name, product_type, current_balance, investor_id, created_at)
VALUES ('Enviro365 Preservation Fund', 'RETIREMENT', 312000.00, 2, CURRENT_TIMESTAMP);

-- Sipho's product
INSERT INTO products (product_name, product_type, current_balance, investor_id, created_at)
VALUES ('Enviro365 Money Market', 'SAVINGS', 89200.75, 3, CURRENT_TIMESTAMP);

-- A prior withdrawal so the history table and CSV export are not empty on
-- first load. Balance snapshots reflect the state at the time of withdrawal.
INSERT INTO withdrawal_notices (reference, amount, balance_before, balance_after, status, product_id, requested_at)
VALUES ('WDR-2026-000001', 15000.00, 140000.00, 125000.00, 'COMPLETED', 1,
        TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP));

INSERT INTO withdrawal_notices (reference, amount, balance_before, balance_after, status, product_id, requested_at)
VALUES ('WDR-2026-000002', 2500.00, 50000.50, 47500.50, 'COMPLETED', 3,
        TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP));