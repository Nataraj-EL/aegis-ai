CREATE TABLE customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    contact_email VARCHAR(255) UNIQUE,
    industry VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    total_revenue NUMERIC(15, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
