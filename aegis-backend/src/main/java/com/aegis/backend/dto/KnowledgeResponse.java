package com.aegis.backend.dto;

import com.aegis.backend.entity.KnowledgeStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeResponse {
    private UUID id;
    private String title;
    private String content;
    private String source;
    private String tags;
    private KnowledgeStatus status;
    private Integer version;
    private String embeddingMetadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
