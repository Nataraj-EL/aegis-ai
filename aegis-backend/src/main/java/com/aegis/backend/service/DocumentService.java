package com.aegis.backend.service;

import com.aegis.backend.entity.Document;

public interface DocumentService {
    Document ingestDocument(String title, String content);
}
