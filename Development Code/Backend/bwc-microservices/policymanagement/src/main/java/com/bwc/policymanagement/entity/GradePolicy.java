package com.bwc.policymanagement.entity;

import com.bwc.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grade_policies", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"policy_id", "grade"}, name = "uk_grade_policy_policy_grade")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class GradePolicy extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String grade;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "companyRate", column = @Column(name = "lodging_company_rate", nullable = false)),
            @AttributeOverride(name = "ownRate", column = @Column(name = "lodging_own_rate", nullable = false))
    })
    private LodgingAllowance lodgingAllowance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "overnightRule", column = @Column(name = "per_diem_overnight_rule", nullable = false, length = 500)),
            @AttributeOverride(name = "dayTripRule", column = @Column(name = "per_diem_day_trip_rule", nullable = false, length = 500))
    })
    private PerDiemAllowance perDiemAllowance;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_policy_policy"))
    private Policy policy;

    @Builder.Default
    @OneToMany(mappedBy = "gradePolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TravelMode> travelModes = new HashSet<>();

    public void addTravelMode(TravelMode travelMode) {
        travelModes.add(travelMode);
        travelMode.setGradePolicy(this);
    }

    public void removeTravelMode(TravelMode travelMode) {
        travelModes.remove(travelMode);
        travelMode.setGradePolicy(null);
    }
}
