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
public class LodgingAllowance {
    @Column(nullable = false)
    private double companyRate;
    
    @Column(nullable = false)
    private double ownRate;
}