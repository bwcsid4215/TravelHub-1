package com.bwc.policymanagement.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyProxyFullDTO {
    private UUID policyId;
    private Integer year;
    private Boolean active;
    private UUID categoryId;
    private String categoryName;

    private List<PolicyGradeDTO> gradePolicies;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PolicyGradeDTO {
        private UUID id;
        private String grade;
        private Double lodgingCompanyRate;
        private Double lodgingOwnRate;
        private String overnightRule;
        private String dayTripRule;
        private List<TravelModeDTO> travelModes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TravelModeDTO {
        private UUID id;
        private String modeName;
        private List<String> allowedClasses;
    }
}
