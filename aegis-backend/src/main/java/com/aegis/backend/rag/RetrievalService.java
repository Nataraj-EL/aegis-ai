package com.aegis.backend.rag;

import java.util.List;

public interface RetrievalService {
    List<String> retrieveContext(String query, int maxResults);
}
