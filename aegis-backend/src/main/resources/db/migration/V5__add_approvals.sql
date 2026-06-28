CREATE TABLE approval_requests (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    requester VARCHAR(50) NOT NULL,
    approver VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    comments VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
