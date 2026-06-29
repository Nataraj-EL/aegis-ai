package com.aegis.backend.dto;

import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;
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
public class TicketResponse {
    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private UUID customerId;
    private String customerName;
    private String assignee;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
