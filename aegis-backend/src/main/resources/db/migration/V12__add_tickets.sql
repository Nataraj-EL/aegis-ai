CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    assignee VARCHAR(100),
    assigned_at TIMESTAMP,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
