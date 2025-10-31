package com.bwc.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Pagination metadata for paginated responses")
public class PaginationMetadata {
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;
    
    @Schema(description = "Number of items per page", example = "10")
    private int size;
    
    @Schema(description = "Total number of items across all pages", example = "150")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "15")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Number of elements on current page", example = "10")
    private int numberOfElements;

    public static PaginationMetadata fromPage(org.springframework.data.domain.Page<?> page) {
        PaginationMetadata metadata = new PaginationMetadata();
        metadata.setPage(page.getNumber());
        metadata.setSize(page.getSize());
        metadata.setTotalElements(page.getTotalElements());
        metadata.setTotalPages(page.getTotalPages());
        metadata.setFirst(page.isFirst());
        metadata.setLast(page.isLast());
        metadata.setNumberOfElements(page.getNumberOfElements());
        return metadata;
    }
}