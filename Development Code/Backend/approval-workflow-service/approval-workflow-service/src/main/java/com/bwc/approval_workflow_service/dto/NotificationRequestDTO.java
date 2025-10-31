package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequestDTO {
    private UUID userId;
    private String userEmail;
    private String subject;
    private String message;
    private String notificationType;
    private UUID referenceId;
    private String referenceType;
}
