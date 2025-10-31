package com.bwc.employee_management_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bwc.employee_management_service.dto.ApiResponse;
import com.bwc.employee_management_service.dto.RoleRequest;
import com.bwc.employee_management_service.dto.RoleResponse;
import com.bwc.employee_management_service.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing employee roles and permissions")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided details")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Role with name already exists")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse createdRole = roleService.createRole(roleRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(createdRole, "Role created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieves a list of all available roles")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "Roles retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieves a specific role by its unique identifier")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
    }

    @GetMapping("/name/{roleName}")
    @Operation(summary = "Get role by name", description = "Retrieves a role by its name (case-insensitive)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @Parameter(description = "Role name", example = "ROLE_ADMIN") @PathVariable String roleName) {
        RoleResponse role = roleService.getRoleByName(roleName);
        return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Updates an existing role's information")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Role name already exists")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {
        
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(role, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Permanently deletes a role from the system")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate role", description = "Deactivates a role making it unavailable for new assignments")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role deactivated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> deactivateRole(
            @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        RoleResponse role = roleService.deactivateRole(id);
        return ResponseEntity.ok(ApiResponse.success(role, "Role deactivated successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate role", description = "Reactivates a previously deactivated role")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role activated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<RoleResponse>> activateRole(
            @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        RoleResponse role = roleService.activateRole(id);
        return ResponseEntity.ok(ApiResponse.success(role, "Role activated successfully"));
    }
}