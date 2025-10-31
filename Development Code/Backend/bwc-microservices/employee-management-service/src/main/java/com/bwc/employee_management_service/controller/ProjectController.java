package com.bwc.employee_management_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bwc.employee_management_service.dto.ApiResponse;
import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.dto.ProjectRequest;
import com.bwc.employee_management_service.dto.ProjectResponse;
import com.bwc.employee_management_service.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "APIs for managing projects and their employee assignments")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Project with name already exists")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(project, "Project created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves a paginated list of all projects")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProjectResponse> projects = projectService.getAllProjects(page, size);
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its unique identifier")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @Parameter(description = "Project UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project, "Project retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Updates an existing project's information")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Project name already exists")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(description = "Project UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id, 
            @Valid @RequestBody ProjectRequest request) {
        
        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(project, "Project updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Permanently deletes a project from the system")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "Project UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search projects by name", description = "Searches for projects by name using partial matching")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects searched successfully")
    })
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> searchProjects(
            @Parameter(description = "Project name or partial name", example = "Mobile") @RequestParam String name) {
        List<ProjectResponse> projects = projectService.searchProjectsByName(name);
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects searched successfully"));
    }
    
    @GetMapping("/{id}/employees")
    @Operation(summary = "Get employees by project", description = "Retrieves all employees assigned to a specific project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByProject(
            @Parameter(description = "Project UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        List<EmployeeResponse> employees = projectService.getEmployeesByProject(id);
        return ResponseEntity.ok(ApiResponse.success(employees, "Employees retrieved successfully"));
    }
}