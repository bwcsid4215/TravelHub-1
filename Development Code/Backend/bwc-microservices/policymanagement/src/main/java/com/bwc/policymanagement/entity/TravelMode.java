package com.bwc.policymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "travel_modes", indexes = {
        @Index(name = "idx_mode_grade", columnList = "modeName, grade_policy_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"gradePolicy", "allowedClasses"})
public class TravelMode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String modeName;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_policy_id", nullable = false)
    private GradePolicy gradePolicy;

    @Builder.Default
    @OneToMany(mappedBy = "travelMode", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TravelClass> allowedClasses = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addTravelClass(TravelClass travelClass) {
        allowedClasses.add(travelClass);
        travelClass.setTravelMode(this);
    }
}
