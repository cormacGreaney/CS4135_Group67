-- First version of the products table. Run automatically when the app starts (Flyway).
-- Product ids are stored as 16-byte UUIDs to match the Java Product class.
CREATE TABLE products (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_products_category (category),
    INDEX idx_products_deleted_at (deleted_at)
);
