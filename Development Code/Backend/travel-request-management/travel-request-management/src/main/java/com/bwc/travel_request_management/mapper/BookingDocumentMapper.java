package com.bwc.travel_request_management.mapper;

import com.bwc.travel_request_management.dto.BookingDocumentDTO;
import com.bwc.travel_request_management.entity.BookingDocument;
import org.springframework.stereotype.Component;

@Component
public class BookingDocumentMapper {

    public BookingDocumentDTO toDto(BookingDocument entity) {
        if (entity == null) return null;
        return BookingDocumentDTO.builder()
                .documentId(entity.getDocumentId())
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .documentType(entity.getDocumentType())
                .description(entity.getDescription())
                .uploadedAt(entity.getUploadedAt())
                .uploadedBy(entity.getUploadedBy())
                .travelBookingId(entity.getTravelBooking() != null ? 
                    entity.getTravelBooking().getBookingId() : null)
                .travelRequestId(entity.getTravelRequest() != null ? 
                    entity.getTravelRequest().getTravelRequestId() : null)
                .build();
    }

    public BookingDocument toEntity(BookingDocumentDTO dto) {
        if (dto == null) return null;
        return BookingDocument.builder()
                .documentId(dto.getDocumentId())
                .fileName(dto.getFileName())
                .originalFileName(dto.getOriginalFileName())
                .fileType(dto.getFileType())
                .fileSize(dto.getFileSize())
                .filePath(dto.getFilePath())
                .documentType(dto.getDocumentType())
                .description(dto.getDescription())
                .uploadedBy(dto.getUploadedBy())
                .build();
    }
}