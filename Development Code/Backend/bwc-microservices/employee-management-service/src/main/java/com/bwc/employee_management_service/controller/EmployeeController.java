package com.bwc.employee_management_service.controller;

import com.bwc.common.dto.ApiResponse;
import com.bwc.common.dto.PaginationMetadata;
import com.bwc.employee_management_service.dto.EmployeeRequest;
import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.dto.ProjectResponse;
import com.bwc.employee_management_service.dto.SearchRequest;
import com.bwc.employee_management_service.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "APIs for managing employees, their projects, and organizational hierarchy")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final HttpServletRequest httpServletRequest;

    @PostMapping
    @Operation(summary = "Create a new employee", description = "Creates a new employee with the provided details including roles, projects, and manager")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Manager, role or project not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Employee with email already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        log.info("Creating new employee: {}", request.getEmail());
        EmployeeResponse employee = employeeService.createEmployee(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(employee, "Employee created successfully",
                        getRequestPath(), getTraceId()));
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieves a paginated list of all employees with sorting options")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @ParameterObject @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {

        log.info("Fetching all employees - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<EmployeeResponse> employees = employeeService.getAllEmployees(pageable);

        ApiResponse<Page<EmployeeResponse>> response = ApiResponse.success(
                employees, "Employees retrieved successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("pagination", PaginationMetadata.fromPage(employees)));

        return ResponseEntity.ok(response);
    }

    // Backward compatibility endpoint
    @GetMapping(params = {"page", "size", "sortBy", "sortDirection"})
    @Operation(summary = "Get all employees (legacy)", description = "Retrieves a paginated list of all employees with legacy parameters", hidden = true)
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployeesLegacy(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "fullName") @RequestParam(defaultValue = "fullName") String sortBy,
            @Parameter(description = "Sort direction", example = "asc") @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<EmployeeResponse> employees = employeeService.getAllEmployees(page, size, sortBy, sortDirection);

        ApiResponse<Page<EmployeeResponse>> response = ApiResponse.success(
                employees, "Employees retrieved successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("pagination", PaginationMetadata.fromPage(employees)));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @Operation(summary = "Search employees", description = "Search employees with advanced filtering, pagination and sorting")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search criteria"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Search service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchEmployees(
            @Parameter(description = "Search criteria") @Valid @RequestBody SearchRequest searchRequest) {

        log.info("Searching employees with criteria: {}", searchRequest);
        Page<EmployeeResponse> employees = employeeService.searchEmployees(searchRequest);

        ApiResponse<Page<EmployeeResponse>> response = ApiResponse.success(
                employees, "Employees search completed successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("pagination", PaginationMetadata.fromPage(employees)));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieves a specific employee by their unique identifier")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {

        log.info("Fetching employee by ID: {}", id);
        EmployeeResponse employee = employeeService.getEmployeeById(id);

        return ResponseEntity.ok(ApiResponse.success(employee, "Employee retrieved successfully",
                getRequestPath(), getTraceId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee's information including roles, projects, and manager")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or self-reference as manager"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id, 
            @Valid @RequestBody EmployeeRequest request) {

        log.info("Updating employee with ID: {}", id);
        EmployeeResponse employee = employeeService.updateEmployee(id, request);

        return ResponseEntity.ok(ApiResponse.success(employee, "Employee updated successfully",
                getRequestPath(), getTraceId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Permanently deletes an employee from the system")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {

        log.info("Deleting employee with ID: {}", id);
        employeeService.deleteEmployee(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully",
                getRequestPath(), getTraceId()));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate employee", description = "Deactivates an employee (soft delete) making them inactive in the system")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee deactivated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> deactivateEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {

        log.info("Deactivating employee with ID: {}", id);
        EmployeeResponse employee = employeeService.deactivateEmployee(id);

        return ResponseEntity.ok(ApiResponse.success(employee, "Employee deactivated successfully",
                getRequestPath(), getTraceId()));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate employee", description = "Reactivates a previously deactivated employee")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee activated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> activateEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {

        log.info("Activating employee with ID: {}", id);
        EmployeeResponse employee = employeeService.activateEmployee(id);

        return ResponseEntity.ok(ApiResponse.success(employee, "Employee activated successfully",
                getRequestPath(), getTraceId()));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get employees by department", description = "Retrieves all employees belonging to a specific department")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByDepartment(
            @Parameter(description = "Department name", example = "Engineering") 
            @PathVariable String department) {

        log.info("Fetching employees by department: {}", department);
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(department);

        ApiResponse<List<EmployeeResponse>> response = ApiResponse.success(
                employees, "Employees retrieved by department successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("count", employees.size(), "department", department));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active employees", description = "Retrieves all currently active employees")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active employees retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getActiveEmployees() {
        log.info("Fetching all active employees");
        List<EmployeeResponse> employees = employeeService.getActiveEmployees();

        ApiResponse<List<EmployeeResponse>> response = ApiResponse.success(
                employees, "Active employees retrieved successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("count", employees.size(), "status", "active"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager/{managerId}/subordinates")
    @Operation(summary = "Get subordinates", description = "Retrieves all employees who report to a specific manager")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subordinates retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Manager not found")
    })
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getSubordinates(
            @Parameter(description = "Manager UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID managerId) {

        log.info("Fetching subordinates for manager ID: {}", managerId);
        List<EmployeeResponse> subordinates = employeeService.getSubordinates(managerId);

        ApiResponse<List<EmployeeResponse>> response = ApiResponse.success(
                subordinates, "Subordinates retrieved successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("count", subordinates.size(), "managerId", managerId));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{employeeId}/assign-projects")
    @Operation(summary = "Assign projects to employee", description = "Assigns multiple projects to an employee")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects assigned successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid project IDs"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee or project not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> assignProjectsToEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID employeeId,
            @Parameter(description = "List of project UUIDs") 
            @RequestBody List<UUID> projectIds) {

        log.info("Assigning {} projects to employee {}", projectIds.size(), employeeId);
        EmployeeResponse employee = employeeService.assignProjectsToEmployee(employeeId, projectIds);

        ApiResponse<EmployeeResponse> response = ApiResponse.success(
                employee, "Projects assigned successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("projectsAssigned", projectIds.size(), "employeeId", employeeId));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{employeeId}/remove-projects")
    @Operation(summary = "Remove projects from employee", description = "Removes multiple projects from an employee")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects removed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid project IDs"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> removeProjectsFromEmployee(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID employeeId,
            @Parameter(description = "List of project UUIDs") 
            @RequestBody List<UUID> projectIds) {

        log.info("Removing {} projects from employee {}", projectIds.size(), employeeId);
        EmployeeResponse employee = employeeService.removeProjectsFromEmployee(employeeId, projectIds);

        ApiResponse<EmployeeResponse> response = ApiResponse.success(
                employee, "Projects removed successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("projectsRemoved", projectIds.size(), "employeeId", employeeId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employeeId}/projects")
    @Operation(summary = "Get employee projects", description = "Retrieves all projects assigned to a specific employee")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getEmployeeProjects(
            @Parameter(description = "Employee UUID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID employeeId) {

        log.info("Fetching projects for employee {}", employeeId);
        List<ProjectResponse> projects = employeeService.getEmployeeProjects(employeeId);

        ApiResponse<List<ProjectResponse>> response = ApiResponse.success(
                projects, "Projects retrieved successfully",
                getRequestPath(), getTraceId()
        ).withMetadata(Map.of("count", projects.size(), "employeeId", employeeId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get employee by email", description = "Retrieves an employee by their email address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee found successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByEmail(
            @Parameter(description = "Email address", example = "john.doe@company.com") 
            @PathVariable String email) {

        log.info("Fetching employee by email: {}", email);
        EmployeeResponse employee = employeeService.getEmployeeByEmail(email);

        return ResponseEntity.ok(ApiResponse.success(employee, "Employee retrieved successfully",
                getRequestPath(), getTraceId()));
    }

    @GetMapping("/health")
    @Operation(summary = "Employee service health check", description = "Provides health information for the employee service", hidden = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthInfo = Map.of(
            "status", "UP",
            "service", "employee-management-service",
            "timestamp", java.time.Instant.now().toString(),
            "activeEmployees", employeeService.getActiveEmployees().size()
        );

        return ResponseEntity.ok(ApiResponse.success(healthInfo, "Service is healthy",
                getRequestPath(), getTraceId()));
    }
    
    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get employees by role", description = "Retrieves all employees having a specific role (e.g., MANAGER, HR, FINANCE)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No employees found with given role")
    })
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByRole(
            @Parameter(description = "Role name", example = "MANAGER") 
            @PathVariable String roleName) {

        log.info("Fetching employees with role: {}", roleName);
        List<EmployeeResponse> employees = employeeService.getEmployeesByRole(roleName);

        if (employees.isEmpty()) {
            throw new com.bwc.common.exception.ResourceNotFoundException("No employees found with role: " + roleName);
        }

        ApiResponse<List<EmployeeResponse>> response = ApiResponse.success(
                employees,
                "Employees retrieved successfully for role: " + roleName,
                getRequestPath(),
                getTraceId()
        ).withMetadata(Map.of("count", employees.size(), "role", roleName));

        return ResponseEntity.ok(response);
    }


    // Helper methods for traceability
    private String getRequestPath() {
        return httpServletRequest.getRequestURI();
    }

    private String getTraceId() {
        // Get from MDC (distributed tracing) or generate new
        String traceId = org.slf4j.MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            org.slf4j.MDC.put("traceId", traceId);
        }
        return traceId;
    }
}
