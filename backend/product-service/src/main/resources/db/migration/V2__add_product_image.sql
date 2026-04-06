-- Product images stored in MySQL. has_image avoids loading LONGBLOB on catalog list queries.
ALTER TABLE products
    ADD COLUMN image_data LONGBLOB NULL,
    ADD COLUMN image_content_type VARCHAR(100) NULL,
    ADD COLUMN has_image TINYINT(1) NOT NULL DEFAULT 0;
