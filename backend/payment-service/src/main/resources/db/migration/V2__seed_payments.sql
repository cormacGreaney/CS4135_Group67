-- Sample payment row for local demos.
-- With a fresh user-service database, the first registered user normally gets id 1,
-- which lets that user fetch this seeded payment through the frontend.
INSERT INTO payments (id, order_id, user_id, amount, provider, status, payment_date)
VALUES
    ('b0000000-0000-4000-8000-000000000001', 5001, 1, 49.99, 'DemoProvider', 'SUCCESS', CURRENT_TIMESTAMP);
