package com.bwc.employee_management_service.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.dto.ProjectRequest;
import com.bwc.employee_management_service.dto.ProjectResponse;
import com.bwc.employee_management_service.entity.Project;
import com.bwc.employee_management_service.exception.ResourceNotFoundException;
import com.bwc.employee_management_service.mapper.EmployeeMapper;
import com.bwc.employee_management_service.mapper.ProjectMapper;
import com.bwc.employee_management_service.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeMapper employeeMapper; // Injected EmployeeMapper

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        log.info("Creating new project: {}", request.getProjectName());

        if (projectRepository.existsByProjectName(request.getProjectName())) {
            throw new IllegalArgumentException("Project with name " + request.getProjectName() + " already exists");
        }

        Project project = Project.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getProjectId());

        return ProjectMapper.toResponse(savedProject);
    }

    public Page<ProjectResponse> getAllProjects(int page, int size) {
        log.info("Fetching all projects - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        return projectRepository.findAll(pageable)
                .map(ProjectMapper::toResponse); // ProjectMapper is assumed static
    }

    public List<ProjectResponse> getAllProjects() {
        log.info("Fetching all projects without pagination");
        return projectRepository.findAll()
                .stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID id) {
        log.info("Fetching project by ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return ProjectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        log.info("Updating project with ID: {}", id);

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!existingProject.getProjectName().equalsIgnoreCase(request.getProjectName()) &&
            projectRepository.existsByProjectName(request.getProjectName())) {
            throw new IllegalArgumentException("Project with name " + request.getProjectName() + " already exists");
        }

        existingProject.setProjectName(request.getProjectName());
        existingProject.setDescription(request.getDescription());

        Project updatedProject = projectRepository.save(existingProject);
        log.info("Project updated successfully with ID: {}", updatedProject.getProjectId());

        return ProjectMapper.toResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID id) {
        log.info("Deleting project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
        log.info("Project deleted successfully with ID: {}", id);
    }

    public List<ProjectResponse> searchProjectsByName(String name) {
        log.info("Searching projects by name: {}", name);
        return projectRepository.findByProjectNameContaining(name)
                .stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByProject(UUID projectId) {
        log.info("Fetching employees for project ID: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Use injected employeeMapper instance instead of static reference
        return project.getEmployees()
                .stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
