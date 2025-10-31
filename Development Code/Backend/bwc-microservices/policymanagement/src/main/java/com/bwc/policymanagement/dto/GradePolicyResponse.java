package com.bwc.policymanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GradePolicyResponse {
    private UUID id;
    private String grade;
    private LodgingAllowanceDTO lodgingAllowance;
    private PerDiemAllowanceDTO perDiemAllowance;
    private List<TravelModeDTO> travelModes;

    @Data
    @Builder
    public static class LodgingAllowanceDTO {
        private Double companyRate;
        private Double ownRate;
    }

    @Data
    @Builder
    public static class PerDiemAllowanceDTO {
        private String overnightRule;
        private String dayTripRule;
    }

    @Data
    @Builder
    public static class TravelModeDTO {
        private UUID id;
        private String modeName;
        private List<TravelClassDTO> allowedClasses;
    }

    @Data
    @Builder
    public static class TravelClassDTO {
        private UUID id;
        private String className;
    }
}
