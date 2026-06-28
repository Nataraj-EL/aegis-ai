CREATE TABLE procurement_requests (
    id UUID PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    estimated_cost NUMERIC(12, 2) NOT NULL,
    justification VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
