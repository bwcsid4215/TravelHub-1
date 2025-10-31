package com.bwc.workflow.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RoleResponseDto {
    private UUID roleId;
    private String roleName;
    private String description;
    private boolean active;
}
