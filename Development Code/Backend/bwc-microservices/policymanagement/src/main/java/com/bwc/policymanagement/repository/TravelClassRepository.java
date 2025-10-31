package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.TravelClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelClassRepository extends JpaRepository<TravelClass, UUID> {
}