package com.bwc.policymanagement.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityResponse {
    private UUID id;
    private String name;
    private String description;
    private CityCategoryResponse category;
}
