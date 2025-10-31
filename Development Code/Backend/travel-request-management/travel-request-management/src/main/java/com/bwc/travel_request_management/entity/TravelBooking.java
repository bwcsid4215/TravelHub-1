package com.bwc.travel_request_management.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "travel_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    private BookingType bookingType;

    @Column(nullable = false, length = 500)
    private String details;

    @Column(length = 1000)
    private String notes;

    // New fields for better booking management
    @Column(name = "booking_reference", length = 100)
    private String bookingReference;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "booking_amount")
    private Double bookingAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "CONFIRMED"; // CONFIRMED, CANCELLED, PENDING

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_request_id", nullable = false)
    private TravelRequest travelRequest;

    // Relationship with BookingDocuments
    @OneToMany(mappedBy = "travelBooking", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BookingDocument> documents = new HashSet<>();

    public enum BookingType {
        FLIGHT, HOTEL, TRAIN, CAR_RENTAL, OTHER
    }

    // Helper methods
    public void addDocument(BookingDocument document) {
        documents.add(document);
        document.setTravelBooking(this);
    }

    public void removeDocument(BookingDocument document) {
        documents.remove(document);
        document.setTravelBooking(null);
    }
}