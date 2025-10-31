package com.bwc.policymanagement.service.impl;

import com.bwc.common.exception.BusinessException;
import com.bwc.common.exception.ResourceNotFoundException;
import com.bwc.policymanagement.dto.*;
import com.bwc.policymanagement.entity.*;
import com.bwc.policymanagement.repository.CityCategoryRepository;
import com.bwc.policymanagement.repository.GradePolicyRepository;
import com.bwc.policymanagement.repository.PolicyRepository;
import com.bwc.policymanagement.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final GradePolicyRepository gradePolicyRepository;
    private final CityCategoryRepository categoryRepository;

    // =================== CRUD Operations ===================

    @Override
    @CacheEvict(value = {"policies", "activePolicies", "policyDetails"}, allEntries = true)
    public Policy createPolicy(PolicyRequest request) {
        log.info("Creating policy for category ID: {} and year: {}", request.getCategoryId(), request.getYear());

        CityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", request.getCategoryId().toString()));

        if (policyRepository.findByCategoryIdAndYear(request.getCategoryId(), request.getYear()).isPresent()) {
            throw new BusinessException("Policy already exists for this category and year");
        }

        Policy policy = Policy.builder()
                .year(request.getYear())
                .active(false)
                .category(category)
                .build();

        request.getGradePolicies().forEach(gradeRequest -> {
            GradePolicy gradePolicy = createGradePolicy(gradeRequest, policy);
            policy.addGradePolicy(gradePolicy);
        });

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Successfully created policy for category: {} with ID: {}", category.getName(), savedPolicy.getId());
        return savedPolicy;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activePolicies", key = "#cityName + '_' + #grade")
    public GradePolicy getActivePolicyByCityAndGrade(String cityName, String grade) {
        validateGrade(grade);
        return gradePolicyRepository.findByCityNameAndGradeAndActivePolicy(cityName, grade.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active policy", "city: " + cityName + ", grade: " + grade));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activePolicies", key = "#cityCategoryId + '_' + #grade")
    public GradePolicy getActivePolicyByCityCategoryAndGrade(UUID cityCategoryId, String grade) {
        validateGrade(grade);
        Policy activePolicy = policyRepository.findByCategoryIdAndActiveTrue(cityCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active policy", "cityCategoryId: " + cityCategoryId));

        return activePolicy.getGradePolicies().stream()
                .filter(gp -> gp.getGrade().equalsIgnoreCase(grade))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Grade policy", "grade: " + grade + " in policy: " + activePolicy.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "policyDetails", key = "#id")
    public Policy getPolicyById(UUID id) {
        return policyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("policies")
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Override
    @CacheEvict(value = {"policies", "activePolicies", "policyDetails"}, allEntries = true)
    public Policy updatePolicy(UUID id, PolicyRequest request) {
        Policy existingPolicy = policyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id.toString()));

        CityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", request.getCategoryId().toString()));

        if (!existingPolicy.getCategory().getId().equals(request.getCategoryId()) ||
                !existingPolicy.getYear().equals(request.getYear())) {
            policyRepository.findByCategoryIdAndYear(request.getCategoryId(), request.getYear())
                    .filter(p -> !p.getId().equals(id))
                    .ifPresent(conflict -> {
                        throw new BusinessException("Policy already exists for this category and year");
                    });
        }

        existingPolicy.setYear(request.getYear());
        existingPolicy.setCategory(category);
        updateGradePolicies(existingPolicy, request.getGradePolicies());

        return policyRepository.save(existingPolicy);
    }

    @Override
    @CacheEvict(value = {"policies", "activePolicies", "policyDetails"}, allEntries = true)
    public void deletePolicy(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id.toString()));
        policyRepository.delete(policy);
    }

    @Override
    @CacheEvict(value = {"policies", "activePolicies", "policyDetails"}, allEntries = true)
    public Policy activatePolicy(UUID id, Boolean active) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id.toString()));

        if (active) {
            deactivateOtherPolicies(policy.getCategory().getId(), id);
            policy.activate();
        } else {
            policy.deactivate();
        }

        Policy savedPolicy = policyRepository.save(policy);

        // Force lazy initialization for nested collections for DTO mapping
        savedPolicy.getGradePolicies().forEach(gp -> gp.getTravelModes().forEach(tm -> tm.getAllowedClasses().size()));

        return savedPolicy;
    }

    // =================== DTO Methods ===================

    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllPoliciesDto() {
        List<Policy> policies = getAllPolicies();
        // Initialize nested collections
        policies.forEach(policy -> policy.getGradePolicies()
                .forEach(gp -> gp.getTravelModes().forEach(tm -> tm.getAllowedClasses().size())));
        return policies.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicyByIdDto(UUID id) {
        Policy policy = getPolicyById(id);
        // Initialize nested collections
        policy.getGradePolicies().forEach(gp -> gp.getTravelModes().forEach(tm -> tm.getAllowedClasses().size()));
        return toDto(policy);
    }

    private PolicyResponse toDto(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .year(policy.getYear())
                .active(policy.isActive())
                .category(PolicyResponse.CategoryInfo.builder()
                        .id(policy.getCategory().getId())
                        .name(policy.getCategory().getName())
                        .description(policy.getCategory().getDescription())
                        .build())
                .gradePolicies(policy.getGradePolicies().stream()
                        .map(gp -> PolicyResponse.GradePolicyInfo.builder()
                                .grade(gp.getGrade())
                                .companyRate(gp.getLodgingAllowance().getCompanyRate())
                                .ownRate(gp.getLodgingAllowance().getOwnRate())
                                .overnightRule(gp.getPerDiemAllowance().getOvernightRule())
                                .dayTripRule(gp.getPerDiemAllowance().getDayTripRule())
                                .travelModes(gp.getTravelModes().stream()
                                        .map(tm -> PolicyResponse.TravelModeInfo.builder()
                                                .modeName(tm.getModeName())
                                                .allowedClasses(tm.getAllowedClasses().stream()
                                                        .map(TravelClass::getClassName)
                                                        .toList())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }

    // =================== Private Helpers ===================

    private void deactivateOtherPolicies(UUID categoryId, UUID currentPolicyId) {
        policyRepository.findByCategoryId(categoryId).stream()
                .filter(Policy::isActive)
                .filter(p -> !p.getId().equals(currentPolicyId))
                .forEach(policy -> {
                    policy.deactivate();
                    policyRepository.save(policy);
                });
    }

    private void updateGradePolicies(Policy policy, List<GradePolicyRequest> gradeRequests) {
        policy.getGradePolicies().clear();
        gradeRequests.forEach(gradeRequest -> {
            GradePolicy gradePolicy = createGradePolicy(gradeRequest, policy);
            policy.addGradePolicy(gradePolicy);
        });
    }

    private GradePolicy createGradePolicy(GradePolicyRequest gradeRequest, Policy policy) {
        validateGrade(gradeRequest.getGrade());

        GradePolicy gradePolicy = GradePolicy.builder()
                .grade(gradeRequest.getGrade().toUpperCase())
                .lodgingAllowance(LodgingAllowance.builder()
                        .companyRate(gradeRequest.getCompanyRate())
                        .ownRate(gradeRequest.getOwnRate())
                        .build())
                .perDiemAllowance(PerDiemAllowance.builder()
                        .overnightRule(gradeRequest.getOvernightRule())
                        .dayTripRule(gradeRequest.getDayTripRule())
                        .build())
                .policy(policy)
                .build();

        gradeRequest.getTravelModes().forEach(tmRequest -> {
            TravelMode tm = createTravelMode(tmRequest, gradePolicy);
            gradePolicy.addTravelMode(tm);
        });

        return gradePolicy;
    }

    private TravelMode createTravelMode(TravelModeRequest travelModeRequest, GradePolicy gradePolicy) {
        TravelMode travelMode = TravelMode.builder()
                .modeName(travelModeRequest.getModeName())
                .gradePolicy(gradePolicy)
                .build();

        travelModeRequest.getAllowedClasses().forEach(className -> {
            TravelClass travelClass = TravelClass.builder()
                    .className(className.toUpperCase())
                    .travelMode(travelMode)
                    .build();
            travelMode.addTravelClass(travelClass);
        });

        return travelMode;
    }

    private void validateGrade(String grade) {
        if (!grade.matches("L[1-5]")) {
            throw new BusinessException("Invalid grade format. Grade must be L1 to L5");
        }
    }
}
