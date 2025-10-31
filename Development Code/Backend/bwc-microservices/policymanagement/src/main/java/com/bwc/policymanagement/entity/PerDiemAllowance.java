package com.bwc.policymanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerDiemAllowance {
    @Column(nullable = false, length = 500)
    private String overnightRule;
    
    @Column(nullable = false, length = 500)
    private String dayTripRule;
}