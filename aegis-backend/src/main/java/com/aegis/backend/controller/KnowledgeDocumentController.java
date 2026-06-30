package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.KnowledgeCreateRequest;
import com.aegis.backend.dto.KnowledgeResponse;
import com.aegis.backend.entity.KnowledgeStatus;
import com.aegis.backend.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Knowledge Base Controller", description = "REST endpoints for managing corporate knowledge base documents")
@RestController
@RequestMapping("/api/v1/knowledge")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class KnowledgeDocumentController {

    private static final String CODE_200 = "200";

    private final KnowledgeDocumentService documentService;

    public KnowledgeDocumentController(final KnowledgeDocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Ingest a knowledge base document",
            description = "Ingests a new document, asynchronously triggering vector indexing.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Knowledge document ingested successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeResponse>> createDocument(
            @Valid @RequestBody final KnowledgeCreateRequest request) {
        final KnowledgeResponse response = documentService.createDocument(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Knowledge document ingested successfully"));
    }

    @Operation(
            summary = "Update knowledge base document",
            description = "Updates document metadata or body content, re-indexing embeddings if content changes.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Knowledge document updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Optimistic locking violation - document was modified concurrently"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeResponse>> updateDocument(
            @PathVariable final UUID id, @Valid @RequestBody final KnowledgeCreateRequest request) {
        final KnowledgeResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Knowledge document updated successfully"));
    }

    @Operation(
            summary = "List knowledge base documents",
            description = "Retrieves knowledge base documents with optional filtering parameters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Knowledge documents loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<KnowledgeResponse>>> getDocuments(
            @RequestParam(required = false) final KnowledgeStatus status,
            @RequestParam(required = false) final String source,
            @RequestParam(required = false) final String title,
            @RequestParam(required = false) final String tag) {
        final List<KnowledgeResponse> response = documentService.getDocuments(status, source, title, tag);
        return ResponseEntity.ok(ApiResponse.success(response, "Knowledge documents loaded successfully"));
    }

    @Operation(
            summary = "Get single knowledge document details",
            description = "Retrieves details for a specific knowledge document.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Knowledge document details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Knowledge document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeResponse>> getDocument(@PathVariable final UUID id) {
        final KnowledgeResponse response = documentService.getDocument(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Knowledge document details loaded successfully"));
    }

    @Operation(summary = "Soft delete a knowledge document", description = "Transitions document status to ARCHIVED.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Knowledge document soft deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Knowledge document not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable final UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Knowledge document soft deleted successfully"));
    }
}
