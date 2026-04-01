-- Sample rows so local dev and demos have something to show in the catalog.
INSERT INTO products (id, name, description, price, stock_quantity, category, deleted_at, created_at)
VALUES
    (UUID_TO_BIN('a0000000-0000-4000-8000-000000000001'), 'Wireless Mouse', 'Ergonomic 2.4GHz mouse', 29.99, 120, 'Electronics', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('a0000000-0000-4000-8000-000000000002'), 'USB-C Cable', '1m braided cable', 12.50, 300, 'Accessories', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('a0000000-0000-4000-8000-000000000003'), 'Notebook A5', 'Lined 200 pages', 8.00, 45, 'Stationery', NULL, CURRENT_TIMESTAMP(6));
