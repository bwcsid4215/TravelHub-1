package com.bwc.travel_request_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "travel_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID travelRequestId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "uuid")
    private UUID employeeId;

    @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    private UUID projectId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 1000)
    private String purpose;

    @Column(name = "manager_present", nullable = false)
    @Builder.Default
    private boolean managerPresent = true;
    
    @Column(name = "manager_id", columnDefinition = "uuid")
    private UUID managerId;

    // --- New fields ---
    @Column(name = "estimated_budget")
    private Double estimatedBudget;

    @Column(name = "travel_destination", length = 255)
    private String travelDestination;;

    @Column(name = "origin", length = 255)
    private String origin;
    // ------------------

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "travelRequest", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TravelExpense> expenses = new HashSet<>();

    @OneToMany(mappedBy = "travelRequest", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TravelBooking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "travelRequest", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TravelAttachment> attachments = new HashSet<>();
    
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "DRAFT";
    
 // Add this to your existing TravelRequest entity
    @OneToMany(mappedBy = "travelRequest", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BookingDocument> bookingDocuments = new HashSet<>();

    // Add helper method
    public void addBookingDocument(BookingDocument document) {
        bookingDocuments.add(document);
        document.setTravelRequest(this);
    }

    public void removeBookingDocument(BookingDocument document) {
        bookingDocuments.remove(document);
        document.setTravelRequest(null);
    }

    // Helper methods for bidirectional relationships
    public void addExpense(TravelExpense expense) {
        expenses.add(expense);
        expense.setTravelRequest(this);
    }

    public void addBooking(TravelBooking booking) {
        bookings.add(booking);
        booking.setTravelRequest(this);
    }

    public void addAttachment(TravelAttachment attachment) {
        attachments.add(attachment);
        attachment.setTravelRequest(this);
    }

    public void removeExpense(TravelExpense expense) {
        expenses.remove(expense);
        expense.setTravelRequest(null);
    }

    public void removeBooking(TravelBooking booking) {
        bookings.remove(booking);
        booking.setTravelRequest(null);
    }

    public void removeAttachment(TravelAttachment attachment) {
        attachments.remove(attachment);
        attachment.setTravelRequest(null);
    }
}
