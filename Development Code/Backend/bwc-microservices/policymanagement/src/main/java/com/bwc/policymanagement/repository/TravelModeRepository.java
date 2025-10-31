package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.TravelMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelModeRepository extends JpaRepository<TravelMode, UUID> {
}