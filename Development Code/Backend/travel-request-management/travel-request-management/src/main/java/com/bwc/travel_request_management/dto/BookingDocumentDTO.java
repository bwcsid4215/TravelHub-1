package com.bwc.travel_request_management.dto;

import com.bwc.travel_request_management.entity.BookingDocument;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDocumentDTO {

    private UUID documentId;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private BookingDocument.DocumentType documentType;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;
    
    private UUID uploadedBy;
    private UUID travelBookingId;
    private UUID travelRequestId;

    // For file download
    private String downloadUrl;
    private String viewUrl;
}