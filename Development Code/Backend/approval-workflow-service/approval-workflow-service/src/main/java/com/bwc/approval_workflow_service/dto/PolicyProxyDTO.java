package com.bwc.approval_workflow_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProxyDTO {
    private UUID policyId;
    private Integer year;
    private Boolean active;
}
