package com.bwc.policymanagement.service.impl;

import com.bwc.policymanagement.dto.PolicyProxyFullDTO;
import com.bwc.policymanagement.entity.*;
import com.bwc.policymanagement.repository.PolicyRepository;
import com.bwc.policymanagement.service.PolicyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyQueryServiceImpl implements PolicyQueryService {

    private final PolicyRepository policyRepository;

    @Override
    @Transactional(readOnly = true)
    public PolicyProxyFullDTO getPolicyByEmployee(UUID employeeId, String grade) {
        // For demo purposes: get any active policy (real impl should map employee -> category)
        Policy p = policyRepository.findByActiveTrue().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No active policy"));

        PolicyProxyFullDTO dto = new PolicyProxyFullDTO();
        dto.setPolicyId(p.getId());
        dto.setYear(p.getYear());
        dto.setActive(p.getActive());
        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());

        dto.setGradePolicies(p.getGradePolicies().stream().map(gp ->
            PolicyProxyFullDTO.PolicyGradeDTO.builder()
                    .id(gp.getId())
                    .grade(gp.getGrade())
                    .lodgingCompanyRate(gp.getLodgingAllowance().getCompanyRate())
                    .lodgingOwnRate(gp.getLodgingAllowance().getOwnRate())
                    .overnightRule(gp.getPerDiemAllowance().getOvernightRule())
                    .dayTripRule(gp.getPerDiemAllowance().getDayTripRule())
                    .travelModes(gp.getTravelModes().stream().map(tm ->
                         PolicyProxyFullDTO.TravelModeDTO.builder()
                             .id(tm.getId())
                             .modeName(tm.getModeName())
                             .allowedClasses(tm.getAllowedClasses().stream().map(tc -> tc.getClassName()).toList())
                             .build()
                    ).collect(Collectors.toList()))
                    .build()
        ).collect(Collectors.toList()));

        if (grade != null && !grade.isBlank()) {
            // Optionally filter by grade
            dto.setGradePolicies(dto.getGradePolicies().stream()
                .filter(g -> g.getGrade().equalsIgnoreCase(grade))
                .collect(Collectors.toList()));
        }

        return dto;
    }
}
