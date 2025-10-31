package com.bwc.travel_request_management.mapper;

import com.bwc.travel_request_management.dto.TravelAttachmentDTO;
import com.bwc.travel_request_management.entity.TravelAttachment;
import org.springframework.stereotype.Component;

@Component
public class TravelAttachmentMapper {

    public TravelAttachmentDTO toDto(TravelAttachment entity) {
        if (entity == null) return null;
        return TravelAttachmentDTO.builder()
                .attachmentId(entity.getAttachmentId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileUrl(entity.getFileUrl())
                .fileSize(entity.getFileSize())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }

    public TravelAttachment toEntity(TravelAttachmentDTO dto) {
        if (dto == null) return null;
        return TravelAttachment.builder()
                .attachmentId(dto.getAttachmentId())
                .fileName(dto.getFileName())
                .fileType(dto.getFileType())
                .fileUrl(dto.getFileUrl())
                .fileSize(dto.getFileSize())
                .build();
    }
}