package com.bwc.employee_management_service.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bwc.common.exception.BusinessException;
import com.bwc.common.exception.ResourceNotFoundException;
import com.bwc.employee_management_service.dto.EmployeeRequest;
import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.dto.ProjectResponse;
import com.bwc.employee_management_service.dto.SearchRequest;
import com.bwc.employee_management_service.entity.Employee;
import com.bwc.employee_management_service.entity.Project;
import com.bwc.employee_management_service.entity.Role;
import com.bwc.employee_management_service.mapper.EmployeeMapper;
import com.bwc.employee_management_service.mapper.ProjectMapper;
import com.bwc.employee_management_service.repository.EmployeeRepository;
import com.bwc.employee_management_service.repository.ProjectRepository;
import com.bwc.employee_management_service.repository.RoleRepository;
import com.bwc.employee_management_service.specification.EmployeeSpecification;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Timed(value = "employee.service", description = "Metrics for Employee Service operations")
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeSpecification employeeSpecification;

    private static final String EMPLOYEE_SERVICE_CIRCUIT_BREAKER = "employeeService";
    private static final String EMPLOYEES_CACHE = "employees";
    private static final String EMPLOYEE_DETAILS_CACHE = "employeeDetails";

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "createEmployeeFallback")
    @Retry(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, allEntries = true)
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating new employee: {}", request.getEmail());
        
        validateEmployeeRequest(request);
        
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Employee with email " + request.getEmail() + " already exists");
        }

        Employee employee = buildEmployeeEntity(request);
        setEmployeeRelationships(employee, request);
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getEmployeeId());
        
        return employeeMapper.toResponse(savedEmployee);
    }

    @Cacheable(value = EMPLOYEES_CACHE, key = "#pageable.getPageNumber() + '-' + #pageable.getPageSize() + '-' + #pageable.getSort()")
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.info("Fetching all employees - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toResponse);
    }

    // Overloaded method for backward compatibility
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return getAllEmployees(pageable);
    }

    @Cacheable(value = EMPLOYEE_DETAILS_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        log.info("Fetching employee by ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id.toString()));
        return employeeMapper.toResponse(employee);
    }

    @Cacheable(value = EMPLOYEE_DETAILS_CACHE, key = "#email")
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {
        log.info("Fetching employee by email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email: " + email));
        return employeeMapper.toResponse(employee);
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "updateEmployeeFallback")
    @Retry(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, allEntries = true)
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);

        validateEmployeeRequest(request);
        
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id.toString()));

        validateEmailUniqueness(request, existingEmployee);
        updateEmployeeFields(existingEmployee, request);
        setEmployeeRelationships(existingEmployee, request);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated successfully with ID: {}", id);
        
        return employeeMapper.toResponse(updatedEmployee);
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "deleteEmployeeFallback")
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, allEntries = true)
    public void deleteEmployee(UUID id) {
        log.info("Deleting employee with ID: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id.toString());
        }

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with ID: {}", id);
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, key = "#id")
    public EmployeeResponse deactivateEmployee(UUID id) {
        log.info("Deactivating employee with ID: {}", id);
        return updateEmployeeStatus(id, false);
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, key = "#id")
    public EmployeeResponse activateEmployee(UUID id) {
        log.info("Activating employee with ID: {}", id);
        return updateEmployeeStatus(id, true);
    }

    @Cacheable(value = EMPLOYEES_CACHE, key = "'department-' + #department")
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(String department) {
        log.info("Fetching employees by department: {}", department);
        return employeeRepository.findByDepartment(department)
                .stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = EMPLOYEES_CACHE, key = "'active'")
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getActiveEmployees() {
        log.info("Fetching all active employees");
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = EMPLOYEES_CACHE, key = "'subordinates-' + #managerId")
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getSubordinates(UUID managerId) {
        log.info("Fetching subordinates for manager ID: {}", managerId);
        return employeeRepository.findByManagerEmployeeId(managerId)
                .stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "assignProjectsFallback")
    @Retry(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, key = "#employeeId")
    public EmployeeResponse assignProjectsToEmployee(UUID employeeId, List<UUID> projectIds) {
        log.info("Assigning {} projects to employee {}", projectIds.size(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId.toString()));

        Set<Project> projects = projectIds.stream()
                .map(projectId -> projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString())))
                .collect(Collectors.toSet());

        employee.getProjects().addAll(projects);
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Assigned {} projects to employee {}", projects.size(), employeeId);
        return employeeMapper.toResponse(updatedEmployee);
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "removeProjectsFallback")
    @Transactional
    @CacheEvict(value = {EMPLOYEES_CACHE, EMPLOYEE_DETAILS_CACHE}, key = "#employeeId")
    public EmployeeResponse removeProjectsFromEmployee(UUID employeeId, List<UUID> projectIds) {
        log.info("Removing {} projects from employee {}", projectIds.size(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId.toString()));

        Set<Project> updatedProjects = employee.getProjects().stream()
                .filter(project -> !projectIds.contains(project.getProjectId()))
                .collect(Collectors.toSet());

        employee.setProjects(updatedProjects);
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Removed {} projects from employee {}", projectIds.size(), employeeId);
        return employeeMapper.toResponse(updatedEmployee);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getEmployeeProjects(UUID employeeId) {
        log.info("Fetching projects for employee {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId.toString()));

        return employee.getProjects().stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "searchEmployeesFallback")
    @Retry(name = EMPLOYEE_SERVICE_CIRCUIT_BREAKER)
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(SearchRequest searchRequest) {
        log.info("Searching employees with criteria: {}", searchRequest);
        
        Specification<Employee> spec = employeeSpecification.buildSearchSpecification(searchRequest);
        Pageable pageable = buildPageable(searchRequest);
        
        return employeeRepository.findAll(spec, pageable)
                .map(employeeMapper::toResponse);
    }

    // Fallback methods
    public EmployeeResponse createEmployeeFallback(EmployeeRequest request, Exception e) {
        log.error("Fallback triggered for createEmployee due to: {}", e.getMessage());
        throw new BusinessException("Employee service temporarily unavailable. Please try again later.");
    }

    public EmployeeResponse updateEmployeeFallback(UUID id, EmployeeRequest request, Exception e) {
        log.error("Fallback triggered for updateEmployee due to: {}", e.getMessage());
        throw new BusinessException("Employee service temporarily unavailable. Please try again later.");
    }

    public void deleteEmployeeFallback(UUID id, Exception e) {
        log.error("Fallback triggered for deleteEmployee due to: {}", e.getMessage());
        throw new BusinessException("Employee service temporarily unavailable. Please try again later.");
    }

    public EmployeeResponse assignProjectsFallback(UUID employeeId, List<UUID> projectIds, Exception e) {
        log.error("Fallback triggered for assignProjects due to: {}", e.getMessage());
        throw new BusinessException("Employee service temporarily unavailable. Please try again later.");
    }

    public EmployeeResponse removeProjectsFallback(UUID employeeId, List<UUID> projectIds, Exception e) {
        log.error("Fallback triggered for removeProjects due to: {}", e.getMessage());
        throw new BusinessException("Employee service temporarily unavailable. Please try again later.");
    }

    public Page<EmployeeResponse> searchEmployeesFallback(SearchRequest searchRequest, Exception e) {
        log.warn("Fallback triggered for employee search, returning empty result");
        return Page.empty();
    }

    // Private helper methods
    private Employee buildEmployeeEntity(EmployeeRequest request) {
        return Employee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .level(request.getLevel())
                .dateJoined(new java.util.Date())
                .isActive(true)
                .build();
    }

    private void setEmployeeRelationships(Employee employee, EmployeeRequest request) {
        setManagerRelationship(employee, request.getManagerId());
        setRoleRelationships(employee, request.getRoleIds());
        setProjectRelationships(employee, request.getProjectIds());
    }

    private void setManagerRelationship(Employee employee, UUID managerId) {
        if (managerId != null) {
            if (managerId.equals(employee.getEmployeeId())) {
                throw new BusinessException("Employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", managerId.toString()));
            employee.setManager(manager);
        }
    }

    private void setRoleRelationships(Employee employee, Set<UUID> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = roleIds.stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString())))
                    .collect(Collectors.toSet());
            employee.setRoles(roles);
        }
    }

    private void setProjectRelationships(Employee employee, Set<UUID> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
            Set<Project> projects = projectIds.stream()
                    .map(projectId -> projectRepository.findById(projectId)
                            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString())))
                    .collect(Collectors.toSet());
            employee.setProjects(projects);
        }
    }

    private void validateEmployeeRequest(EmployeeRequest request) {
        if (request == null) {
            throw new BusinessException("Employee request cannot be null");
        }
        // Add additional validation logic here if needed
    }

    private void validateEmailUniqueness(EmployeeRequest request, Employee existingEmployee) {
        if (request.getEmail() != null 
                && !request.getEmail().equals(existingEmployee.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Employee with email " + request.getEmail() + " already exists");
        }
    }

    private void updateEmployeeFields(Employee employee, EmployeeRequest request) {
        if (request.getFullName() != null) employee.setFullName(request.getFullName());
        if (request.getEmail() != null) employee.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) employee.setPhoneNumber(request.getPhoneNumber());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getLevel() != null) employee.setLevel(request.getLevel());
    }

    private EmployeeResponse updateEmployeeStatus(UUID id, boolean isActive) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id.toString()));

        employee.setIsActive(isActive);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        log.info("Employee {} successfully with ID: {}", 
                isActive ? "activated" : "deactivated", id);
                
        return employeeMapper.toResponse(updatedEmployee);
    }
    
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByRole(String roleName) {
        log.info("Fetching employees by role: {}", roleName);
        return employeeRepository.findByRoleName(roleName.toUpperCase())
                .stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }


    private Pageable buildPageable(SearchRequest searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(
                searchRequest.getSortDirection()), 
                searchRequest.getSortBy()
        );
        return PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                sort
        );
    }
}