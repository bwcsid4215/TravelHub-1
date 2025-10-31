package com.bwc.travel_request_management.controller;

import com.bwc.travel_request_management.dto.TravelAttachmentDTO;
import com.bwc.travel_request_management.service.TravelAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/travel-attachments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Travel Attachments", description = "Manage attachments for travel requests")
public class TravelAttachmentController {

    private final TravelAttachmentService service;

    @Operation(summary = "Add a travel attachment", description = "Attach a file reference to a travel request")
    @PostMapping("/{requestId}")
    public ResponseEntity<TravelAttachmentDTO> add(
            @Parameter(description = "ID of the travel request") @PathVariable UUID requestId,
            @Valid @RequestBody TravelAttachmentDTO dto) {
        return ResponseEntity.ok(service.addAttachment(requestId, dto));
    }

    @Operation(summary = "Get an attachment by ID")
    @GetMapping("/{attachmentId}")
    public ResponseEntity<TravelAttachmentDTO> get(
            @Parameter(description = "ID of the attachment") @PathVariable UUID attachmentId) {
        return ResponseEntity.ok(service.getAttachment(attachmentId));
    }

    @Operation(summary = "List attachments for a request")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<List<TravelAttachmentDTO>> getByRequest(
            @Parameter(description = "ID of the travel request") @PathVariable UUID requestId) {
        return ResponseEntity.ok(service.getAttachmentsForRequest(requestId));
    }

    @Operation(summary = "Delete an attachment by ID")
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the attachment") @PathVariable UUID attachmentId) {
        service.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
