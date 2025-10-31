// src/main/java/com/bwc/approval_workflow_service/dto/PolicyGradeDTO.java
package com.bwc.approval_workflow_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyGradeDTO {
    private UUID id;
    private String grade; // ex: L2
    private LodgingAllowance lodgingAllowance;
    private PerDiemAllowance perDiemAllowance;
    private List<TravelModeDTO> travelModes;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LodgingAllowance {
        private Double companyRate; // representation maybe fraction or rupees depending on policy service
        private Double ownRate;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PerDiemAllowance {
        private String overnightRule;
        private String dayTripRule;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TravelModeDTO {
        private UUID id;
        private String modeName;
        private List<TravelClassDTO> allowedClasses;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TravelClassDTO {
        private UUID id;
        private String className;
    }
}
