package com.bwc.approval_workflow_service.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProxyFullDTO {
    private UUID policyId;
    private Integer year;
    private Boolean active;
    private UUID categoryId;
    private String categoryName;
    private List<PolicyGradeDTO> gradePolicies;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyGradeDTO {
        private UUID id;
        private String grade;
        private LodgingAllowance lodgingAllowance;
        private PerDiemAllowance perDiemAllowance;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LodgingAllowance {
        private Double companyRate;
        private Double ownRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerDiemAllowance {
        private String overnightRule;
        private String dayTripRule;
    }
}