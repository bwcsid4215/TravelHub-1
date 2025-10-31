package com.bwc.travel_request_management.service.impl;

import com.bwc.travel_request_management.dto.TravelBookingDTO;
import com.bwc.travel_request_management.dto.BookingSummaryDTO;
import com.bwc.travel_request_management.entity.TravelBooking;
import com.bwc.travel_request_management.entity.TravelRequest;
import com.bwc.travel_request_management.exception.ResourceNotFoundException;
import com.bwc.travel_request_management.mapper.TravelBookingMapper;
import com.bwc.travel_request_management.repository.TravelBookingRepository;
import com.bwc.travel_request_management.repository.TravelRequestRepository;
import com.bwc.travel_request_management.service.TravelBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelBookingServiceImpl implements TravelBookingService {

    private final TravelBookingRepository bookingRepository;
    private final TravelRequestRepository requestRepository;
    private final TravelBookingMapper mapper;

    @Override
    @Transactional
    public TravelBookingDTO addBooking(UUID requestId, TravelBookingDTO bookingDto) {
        TravelRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel Request not found with id: " + requestId));

        TravelBooking booking = mapper.toEntity(bookingDto);
        booking.setTravelRequest(request);
        
        // Set default values
        if (booking.getStatus() == null) {
            booking.setStatus("CONFIRMED");
        }
        if (booking.getCurrency() == null) {
            booking.setCurrency("INR");
        }
        if (booking.getBookingDate() == null) {
            booking.setBookingDate(java.time.LocalDateTime.now());
        }

        TravelBooking saved = bookingRepository.save(booking);
        log.info("Booking added successfully: {} for request: {}", saved.getBookingId(), requestId);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelBookingDTO getBooking(UUID id) {
        return bookingRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelBookingDTO> getBookingsForRequest(UUID requestId) {
        return bookingRepository.findByTravelRequest_TravelRequestId(requestId)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBooking(UUID bookingId) {
        TravelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        bookingRepository.delete(booking);
        log.info("Booking deleted: {}", bookingId);
    }

    @Override
    @Transactional
    public TravelBookingDTO updateBookingStatus(UUID bookingId, String status) {
        TravelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        booking.setStatus(status);
        TravelBooking updated = bookingRepository.save(booking);
        
        log.info("Booking status updated: {} -> {}", bookingId, status);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSummaryDTO getBookingSummary(UUID requestId) {
        List<TravelBookingDTO> bookings = getBookingsForRequest(requestId);
        
        Double totalAmount = bookings.stream()
                .filter(b -> b.getBookingAmount() != null)
                .mapToDouble(TravelBookingDTO::getBookingAmount)
                .sum();

        return BookingSummaryDTO.builder()
                .travelRequestId(requestId)
                .totalBookings(bookings.size())
                .totalBookingAmount(totalAmount)
                .bookings(bookings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalBookingAmount(UUID requestId) {
        return bookingRepository.findByTravelRequest_TravelRequestId(requestId)
                .stream()
                .filter(b -> b.getBookingAmount() != null)
                .mapToDouble(TravelBooking::getBookingAmount)
                .sum();
    }
}