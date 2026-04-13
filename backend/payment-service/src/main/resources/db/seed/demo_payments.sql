-- Idempotent demo payment (same row V2 used to insert). Toggle via app.payments.seed.enabled=false to skip.
INSERT INTO payments (id, order_id, user_id, amount, provider, status, payment_date)
VALUES (
    'b0000000-0000-4000-8000-000000000001'::uuid,
    5001,
    1,
    49.99,
    'DemoProvider',
    'SUCCESS',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
