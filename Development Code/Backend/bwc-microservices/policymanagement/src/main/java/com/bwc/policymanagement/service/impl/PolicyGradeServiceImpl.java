package com.bwc.policymanagement.service.impl;

import com.bwc.common.exception.BusinessException;
import com.bwc.common.exception.ResourceNotFoundException;
import com.bwc.policymanagement.dto.AddGradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyResponse;
import com.bwc.policymanagement.entity.*;
import com.bwc.policymanagement.repository.CityCategoryRepository;
import com.bwc.policymanagement.repository.PolicyRepository;
import com.bwc.policymanagement.service.PolicyGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyGradeServiceImpl implements PolicyGradeService {

    private final PolicyRepository policyRepository;
    private final CityCategoryRepository categoryRepository;

    @Override
    public GradePolicyResponse addGradePolicy(AddGradePolicyRequest request) {
        CityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", request.getCategoryId().toString()));

        Policy policy = policyRepository.findByCategoryIdAndYear(category.getId(), request.getYear())
                .orElseGet(() -> {
                    Policy newPolicy = Policy.builder()
                            .year(request.getYear())
                            .active(false)
                            .category(category)
                            .build();
                    return policyRepository.saveAndFlush(newPolicy);
                });

        policy.getGradePolicies().stream()
                .filter(gp -> gp.getGrade().equalsIgnoreCase(request.getGradePolicy().getGrade()))
                .findAny()
                .ifPresent(gp -> {
                    throw new BusinessException("Grade " + gp.getGrade() + " already exists in this policy");
                });

        GradePolicy gradePolicy = createGradePolicy(request.getGradePolicy(), policy);
        policy.addGradePolicy(gradePolicy);

        Policy savedPolicy = policyRepository.saveAndFlush(policy);

        GradePolicy persistedGradePolicy = savedPolicy.getGradePolicies().stream()
                .filter(gp -> gp.getGrade().equalsIgnoreCase(gradePolicy.getGrade()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("GradePolicy not saved properly"));

        return toResponseDto(persistedGradePolicy);
    }

    @Override
    public GradePolicyResponse updateGradePolicy(UUID policyId, String grade, GradePolicyRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId.toString()));

        GradePolicy gradePolicy = policy.getGradePolicies().stream()
                .filter(gp -> gp.getGrade().equalsIgnoreCase(grade))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("GradePolicy", grade));

        // Update fields
        gradePolicy.setGrade(request.getGrade().toUpperCase());
        gradePolicy.setLodgingAllowance(LodgingAllowance.builder()
                .companyRate(request.getCompanyRate())
                .ownRate(request.getOwnRate())
                .build());
        gradePolicy.setPerDiemAllowance(PerDiemAllowance.builder()
                .overnightRule(request.getOvernightRule())
                .dayTripRule(request.getDayTripRule())
                .build());

        gradePolicy.getTravelModes().clear();
        request.getTravelModes().forEach(tmReq -> {
            TravelMode tm = TravelMode.builder()
                    .modeName(tmReq.getModeName())
                    .gradePolicy(gradePolicy)
                    .build();
            tmReq.getAllowedClasses().forEach(cls -> tm.addTravelClass(
                    TravelClass.builder().className(cls.toUpperCase()).travelMode(tm).build()
            ));
            gradePolicy.addTravelMode(tm);
        });

        policyRepository.saveAndFlush(policy);
        return toResponseDto(gradePolicy);
    }

    @Override
    public void deleteGradePolicy(UUID policyId, String grade) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId.toString()));

        GradePolicy gradePolicy = policy.getGradePolicies().stream()
                .filter(gp -> gp.getGrade().equalsIgnoreCase(grade))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("GradePolicy", grade));

        policy.removeGradePolicy(gradePolicy);
        policyRepository.saveAndFlush(policy);
    }

    @Override
    public List<GradePolicyResponse> getGradePolicies(UUID policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId.toString()));

        return policy.getGradePolicies().stream()
                .map(this::toResponseDto)
                .toList();
    }

    private GradePolicy createGradePolicy(GradePolicyRequest request, Policy policy) {
        GradePolicy gradePolicy = GradePolicy.builder()
                .grade(request.getGrade().toUpperCase())
                .lodgingAllowance(LodgingAllowance.builder()
                        .companyRate(request.getCompanyRate())
                        .ownRate(request.getOwnRate())
                        .build())
                .perDiemAllowance(PerDiemAllowance.builder()
                        .overnightRule(request.getOvernightRule())
                        .dayTripRule(request.getDayTripRule())
                        .build())
                .policy(policy)
                .build();

        request.getTravelModes().forEach(tmReq -> {
            TravelMode tm = TravelMode.builder()
                    .modeName(tmReq.getModeName())
                    .gradePolicy(gradePolicy)
                    .build();
            tmReq.getAllowedClasses().forEach(cls -> tm.addTravelClass(
                    TravelClass.builder().className(cls.toUpperCase()).travelMode(tm).build()
            ));
            gradePolicy.addTravelMode(tm);
        });

        return gradePolicy;
    }

    private GradePolicyResponse toResponseDto(GradePolicy gp) {
        return GradePolicyResponse.builder()
                .id(gp.getId())
                .grade(gp.getGrade())
                .lodgingAllowance(GradePolicyResponse.LodgingAllowanceDTO.builder()
                        .companyRate(gp.getLodgingAllowance().getCompanyRate())
                        .ownRate(gp.getLodgingAllowance().getOwnRate())
                        .build())
                .perDiemAllowance(GradePolicyResponse.PerDiemAllowanceDTO.builder()
                        .overnightRule(gp.getPerDiemAllowance().getOvernightRule())
                        .dayTripRule(gp.getPerDiemAllowance().getDayTripRule())
                        .build())
                .travelModes(gp.getTravelModes().stream()
                        .map(tm -> GradePolicyResponse.TravelModeDTO.builder()
                                .id(tm.getId())
                                .modeName(tm.getModeName())
                                .allowedClasses(tm.getAllowedClasses().stream()
                                        .map(tc -> GradePolicyResponse.TravelClassDTO.builder()
                                                .id(tc.getId())
                                                .className(tc.getClassName())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }
}
