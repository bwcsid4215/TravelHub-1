package com.bwc.policymanagement.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PolicyResponse {

    private UUID id;
    private int year;
    private boolean active;
    private CategoryInfo category;
    private List<GradePolicyInfo> gradePolicies;

    @Getter
    @Setter
    @Builder
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String description;
    }

    @Getter
    @Setter
    @Builder
    public static class GradePolicyInfo {
        private String grade;
        private double companyRate;
        private double ownRate;
        private String overnightRule;
        private String dayTripRule;
        private List<TravelModeInfo> travelModes;
    }

    @Getter
    @Setter
    @Builder
    public static class TravelModeInfo {
        private String modeName;
        private List<String> allowedClasses;
    }
}
