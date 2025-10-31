package com.bwc.travel_request_management.service.impl;

import com.bwc.travel_request_management.dto.TravelAttachmentDTO;
import com.bwc.travel_request_management.entity.TravelAttachment;
import com.bwc.travel_request_management.entity.TravelRequest;
import com.bwc.travel_request_management.exception.ResourceNotFoundException;
import com.bwc.travel_request_management.mapper.TravelAttachmentMapper;
import com.bwc.travel_request_management.repository.TravelAttachmentRepository;
import com.bwc.travel_request_management.repository.TravelRequestRepository;
import com.bwc.travel_request_management.service.TravelAttachmentService;
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
public class TravelAttachmentServiceImpl implements TravelAttachmentService {

    private final TravelAttachmentRepository attachmentRepository;
    private final TravelRequestRepository requestRepository;
    private final TravelAttachmentMapper mapper;

    @Override
    @Transactional
    public TravelAttachmentDTO addAttachment(UUID requestId, TravelAttachmentDTO attachmentDto) {
        TravelRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel Request not found with id: " + requestId));

        TravelAttachment attachment = mapper.toEntity(attachmentDto);
        attachment.setTravelRequest(request);
        TravelAttachment saved = attachmentRepository.save(attachment);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelAttachmentDTO getAttachment(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelAttachmentDTO> getAttachmentsForRequest(UUID requestId) {
        return attachmentRepository.findByTravelRequest_TravelRequestId(requestId)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        if (!attachmentRepository.existsById(attachmentId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId);
        }
        attachmentRepository.deleteById(attachmentId);
    }
}