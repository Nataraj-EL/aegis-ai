CREATE TABLE vendors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    contact_email VARCHAR(255),
    category VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    rating NUMERIC(3, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE procurement_requests ADD COLUMN vendor_id UUID REFERENCES vendors(id);
