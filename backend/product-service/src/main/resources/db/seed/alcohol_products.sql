-- Optional alcohol catalog seed (loaded at startup when app.catalog.seed-alcohol.enabled=true).
-- INSERT IGNORE keeps this idempotent if the app restarts against the same database.
INSERT IGNORE INTO products (id, name, description, price, stock_quantity, category, deleted_at, created_at)
VALUES
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000011'), 'Guinness Draught Stout', 'Irish dry stout, 4.2% ABV, 440ml can', 2.20, 200, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000012'), 'Heineken Lager', 'Dutch pale lager, 5.0% ABV, 330ml bottle', 1.85, 320, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000013'), 'Corona Extra', 'Mexican pale lager with lime, 4.5% ABV, 330ml bottle', 2.10, 280, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000014'), 'Sierra Nevada Pale Ale', 'American pale ale, 5.6% ABV, 355ml can', 2.65, 150, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000015'), 'Stella Artois Lager', 'Belgian pilsner-style lager, 5.0% ABV, 330ml bottle', 2.05, 260, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000016'), 'Yellow Tail Shiraz', 'Australian red wine, 13.5% ABV, 750ml bottle', 7.99, 85, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000017'), 'Barefoot Pinot Grigio', 'California white wine, 12.5% ABV, 750ml bottle', 6.49, 90, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000018'), 'Moët & Chandon Impérial Brut', 'French champagne, 12% ABV, 750ml bottle', 42.00, 24, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-000000000019'), 'Smirnoff No.21 Vodka', 'Triple distilled vodka, 37.5% ABV, 700ml bottle', 16.50, 55, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-00000000001a'), 'Tanqueray London Dry Gin', 'Classic London dry gin, 43.1% ABV, 700ml bottle', 22.99, 40, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-00000000001b'), 'Captain Morgan Original Spiced Gold', 'Caribbean spiced rum, 35% ABV, 700ml bottle', 18.75, 48, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-00000000001c'), 'Jack Daniel''s Old No.7', 'Tennessee whiskey, 40% ABV, 700ml bottle', 28.00, 35, 'Alcohol', NULL, CURRENT_TIMESTAMP(6)),
    (UUID_TO_BIN('b0000000-0000-4000-8000-00000000001e'), 'Baileys Irish Cream', 'Cream liqueur, 17% ABV, 700ml bottle', 17.25, 60, 'Alcohol', NULL, CURRENT_TIMESTAMP(6));
