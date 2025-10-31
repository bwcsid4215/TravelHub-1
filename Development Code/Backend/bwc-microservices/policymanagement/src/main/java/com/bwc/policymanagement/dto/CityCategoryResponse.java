package com.bwc.policymanagement.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityCategoryResponse {
    private UUID id;
    private String name;
    private String description;
}
