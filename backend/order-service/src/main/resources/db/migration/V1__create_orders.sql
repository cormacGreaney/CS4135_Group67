-- Order service schema (aligned with JPA entities Order / OrderItem)

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(255),
    total_price DECIMAL(19, 2),
    status VARCHAR(50),
    ordered_date DATETIME(6)
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    product_name VARCHAR(255),
    price DECIMAL(19, 2),
    quantity INT,
    order_id BIGINT,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
