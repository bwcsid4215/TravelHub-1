package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.dto.TravelAttachmentDTO;

import java.util.List;
import java.util.UUID;

public interface TravelAttachmentService {
    TravelAttachmentDTO addAttachment(UUID requestId, TravelAttachmentDTO attachmentDto);
    TravelAttachmentDTO getAttachment(UUID attachmentId);
    List<TravelAttachmentDTO> getAttachmentsForRequest(UUID requestId);
    void deleteAttachment(UUID attachmentId);
}
