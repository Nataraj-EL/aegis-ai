CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source VARCHAR(255),
    tags VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE knowledge_embeddings (
    document_id UUID PRIMARY KEY REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    embedding vector(768) NOT NULL,
    model_version VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_knowledge_docs_status ON knowledge_documents(status);
CREATE INDEX idx_knowledge_embs_embedding ON knowledge_embeddings USING hnsw (embedding vector_cosine_ops);
