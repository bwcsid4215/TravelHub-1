// employee-management-service/src/main/java/com/bwc/employee_management_service/specification/EmployeeSpecification.java
package com.bwc.employee_management_service.specification;

import com.bwc.employee_management_service.dto.SearchRequest;
import com.bwc.employee_management_service.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeSpecification {

    public Specification<Employee> buildSearchSpecification(SearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search across multiple fields
            if (StringUtils.hasText(searchRequest.getKeyword())) {
                String keywordPattern = "%" + searchRequest.getKeyword().toLowerCase() + "%";
                Predicate keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), keywordPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), keywordPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), keywordPattern)
                );
                predicates.add(keywordPredicate);
            }

            // Department filter
            if (StringUtils.hasText(searchRequest.getDepartment())) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("department")), 
                    searchRequest.getDepartment().toLowerCase()
                ));
            }

            // Active status filter
            if (searchRequest.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), searchRequest.getActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}