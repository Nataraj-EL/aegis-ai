package com.aegis.backend.dto;

import com.aegis.backend.entity.KnowledgeStatus;

public interface KnowledgeMetricsProjection {
    KnowledgeStatus getStatus();

    Long getCount();
}
