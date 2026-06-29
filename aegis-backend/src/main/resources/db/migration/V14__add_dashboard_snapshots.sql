CREATE TABLE dashboard_snapshots (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary_data jsonb NOT NULL,
    created_at TIMESTAMP NOT NULL
);
