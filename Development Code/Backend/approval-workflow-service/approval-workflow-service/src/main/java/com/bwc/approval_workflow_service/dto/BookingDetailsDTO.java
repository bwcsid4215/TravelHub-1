package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDetailsDTO {
    private List<FlightBookingDTO> flightBookings;
    private List<HotelBookingDTO> hotelBookings;
    private List<CarRentalDTO> carRentals;
    private List<OtherBookingDTO> otherBookings;
    private Double totalBookingAmount;
    private String bookingNotes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightBookingDTO {
        private String airline;
        private String flightNumber;
        private String departureAirport;
        private String arrivalAirport;
        private String departureDate;
        private String arrivalDate;
        private Double amount;
        private String bookingReference;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelBookingDTO {
        private String hotelName;
        private String location;
        private String checkInDate;
        private String checkOutDate;
        private Integer numberOfNights;
        private Double amount;
        private String bookingReference;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarRentalDTO {
        private String rentalCompany;
        private String carType;
        private String pickupDate;
        private String dropoffDate;
        private String pickupLocation;
        private Double amount;
        private String bookingReference;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherBookingDTO {
        private String type;
        private String description;
        private String date;
        private Double amount;
        private String bookingReference;
        private String status;
    }
}