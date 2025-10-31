package com.bwc.workflow.client.dto;

import lombok.Data;

@Data
public class GradePolicyDto {
    private String grade;
    private Double companyRate;
    private Double ownRate;
    private String overnightRule;
    private String dayTripRule;
}
