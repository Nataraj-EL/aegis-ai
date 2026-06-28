CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    request_id VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    execution_time BIGINT,
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT
);
