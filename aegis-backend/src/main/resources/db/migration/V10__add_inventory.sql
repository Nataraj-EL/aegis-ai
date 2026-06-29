CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    reorder_threshold INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
