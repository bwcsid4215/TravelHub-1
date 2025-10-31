package com.bwc.travel_request_management.service.impl;

import com.bwc.travel_request_management.client.WorkflowServiceClient;
import com.bwc.travel_request_management.dto.BookingDocumentDTO;
import com.bwc.travel_request_management.entity.BookingDocument;
import com.bwc.travel_request_management.entity.TravelBooking;
import com.bwc.travel_request_management.entity.TravelRequest;
import com.bwc.travel_request_management.exception.FileStorageException;
import com.bwc.travel_request_management.exception.ResourceNotFoundException;
import com.bwc.travel_request_management.mapper.BookingDocumentMapper;
import com.bwc.travel_request_management.repository.BookingDocumentRepository;
import com.bwc.travel_request_management.repository.TravelBookingRepository;
import com.bwc.travel_request_management.repository.TravelRequestRepository;
import com.bwc.travel_request_management.service.BookingDocumentService;
import com.bwc.travel_request_management.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingDocumentServiceImpl implements BookingDocumentService {

    private final BookingDocumentRepository documentRepository;
    private final TravelBookingRepository bookingRepository;
    private final TravelRequestRepository requestRepository;
    private final FileStorageService fileStorageService;
    private final BookingDocumentMapper mapper;
    private final WorkflowServiceClient workflowServiceClient;

    @Override
    @Transactional
    public BookingDocumentDTO uploadBookingDocument(UUID bookingId, MultipartFile file, 
                                                   String documentType, String description, UUID uploadedBy) {
        
        TravelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel Booking not found with id: " + bookingId));

        TravelRequest travelRequest = booking.getTravelRequest();

        // Validate file
        validateFile(file);

        // Store file physically
        String storedFileName = fileStorageService.storeFile(file, travelRequest.getTravelRequestId(), documentType);

        // Create document entity
        BookingDocument document = BookingDocument.builder()
                .fileName(storedFileName)
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(storedFileName)
                .documentType(BookingDocument.DocumentType.valueOf(documentType))
                .description(description)
                .uploadedBy(uploadedBy)
                .travelBooking(booking)
                .travelRequest(travelRequest)
                .build();

        BookingDocument savedDocument = documentRepository.save(document);
        
        // Add to booking's document collection
        booking.addDocument(savedDocument);

        log.info("Booking document uploaded successfully: {} for booking: {}", storedFileName, bookingId);

        // Generate download URLs
        BookingDocumentDTO dto = mapper.toDto(savedDocument);
        dto.setDownloadUrl(generateDownloadUrl(savedDocument.getDocumentId()));
        dto.setViewUrl(generateViewUrl(savedDocument.getDocumentId()));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDocumentDTO getDocument(UUID documentId) {
        BookingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking document not found with id: " + documentId));
        
        BookingDocumentDTO dto = mapper.toDto(document);
        dto.setDownloadUrl(generateDownloadUrl(documentId));
        dto.setViewUrl(generateViewUrl(documentId));
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDocumentDTO> getDocumentsByBooking(UUID bookingId) {
        return documentRepository.findByTravelBooking_BookingId(bookingId)
                .stream()
                .map(document -> {
                    BookingDocumentDTO dto = mapper.toDto(document);
                    dto.setDownloadUrl(generateDownloadUrl(document.getDocumentId()));
                    dto.setViewUrl(generateViewUrl(document.getDocumentId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDocumentDTO> getDocumentsByRequest(UUID requestId) {
        return documentRepository.findByTravelRequest_TravelRequestId(requestId)
                .stream()
                .map(document -> {
                    BookingDocumentDTO dto = mapper.toDto(document);
                    dto.setDownloadUrl(generateDownloadUrl(document.getDocumentId()));
                    dto.setViewUrl(generateViewUrl(document.getDocumentId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDocumentDTO> getDocumentsByType(UUID requestId, String documentType) {
        return documentRepository.findByTravelRequest_TravelRequestIdAndDocumentType(
                requestId, BookingDocument.DocumentType.valueOf(documentType))
                .stream()
                .map(document -> {
                    BookingDocumentDTO dto = mapper.toDto(document);
                    dto.setDownloadUrl(generateDownloadUrl(document.getDocumentId()));
                    dto.setViewUrl(generateViewUrl(document.getDocumentId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId) {
        BookingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking document not found with id: " + documentId));

        // Delete physical file
        fileStorageService.deleteFile(document.getFileName());

        // Remove from booking's collection
        TravelBooking booking = document.getTravelBooking();
        booking.getDocuments().remove(document);

        // Delete database record
        documentRepository.delete(document);
        log.info("Booking document deleted: {}", documentId);
    }

    @Override
    @Transactional
    public void deleteAllDocumentsForBooking(UUID bookingId) {
        List<BookingDocument> documents = documentRepository.findByTravelBooking_BookingId(bookingId);
        
        for (BookingDocument document : documents) {
            fileStorageService.deleteFile(document.getFileName());
        }
        
        documentRepository.deleteAll(documents);
        log.info("All booking documents deleted for booking: {}", bookingId);
    }

    @Override
    @Transactional
    public void deleteAllDocumentsForRequest(UUID requestId) {
        List<BookingDocument> documents = documentRepository.findByTravelRequest_TravelRequestId(requestId);
        
        for (BookingDocument document : documents) {
            fileStorageService.deleteFile(document.getFileName());
        }
        
        documentRepository.deleteAll(documents);
        log.info("All booking documents deleted for request: {}", requestId);
    }

    @Override
    public Resource downloadDocument(UUID documentId) {
        BookingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking document not found with id: " + documentId));

        // FIXED: Pass travelRequestId to loadFileAsResource
        return fileStorageService.loadFileAsResource(
            document.getTravelRequest().getTravelRequestId(), 
            document.getFileName()
        );
    }

    @Override
    public String getDocumentContentType(UUID documentId) {
        BookingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking document not found with id: " + documentId));
        
        return document.getFileType();
    }

    @Override
    public String getDocumentOriginalName(UUID documentId) {
        BookingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking document not found with id: " + documentId));
        
        return document.getOriginalFileName();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new FileStorageException("File size exceeds maximum limit of 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && 
             !contentType.equals("application/pdf") &&
             !contentType.equals("application/msword") &&
             !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new FileStorageException("Only images, PDF, and Word documents are allowed.");
        }
    }

    private String generateDownloadUrl(UUID documentId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/booking-documents/")
                .path(documentId.toString())
                .path("/download")
                .toUriString();
    }

    private String generateViewUrl(UUID documentId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/booking-documents/")
                .path(documentId.toString())
                .path("/view")
                .toUriString();
    }
    
    
}