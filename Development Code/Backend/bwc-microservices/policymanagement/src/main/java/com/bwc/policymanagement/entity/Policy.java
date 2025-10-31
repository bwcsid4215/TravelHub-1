package com.bwc.policymanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import com.bwc.common.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "policies", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "year"}, name = "uk_policy_category_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class Policy extends BaseEntity {

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policy_category"))
    private CityCategory category;

    @Builder.Default
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GradePolicy> gradePolicies = new HashSet<>();

    public void addGradePolicy(GradePolicy gradePolicy) {
        gradePolicies.add(gradePolicy);
        gradePolicy.setPolicy(this);
    }

    public void removeGradePolicy(GradePolicy gradePolicy) {
        gradePolicies.remove(gradePolicy);
        gradePolicy.setPolicy(null);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
