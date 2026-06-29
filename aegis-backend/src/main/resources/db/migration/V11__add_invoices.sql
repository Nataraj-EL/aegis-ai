CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    deal_id UUID REFERENCES deals(id),
    due_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
