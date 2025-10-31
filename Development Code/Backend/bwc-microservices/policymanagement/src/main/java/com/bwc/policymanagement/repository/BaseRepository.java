//// src/main/java/com/bwc/policymanagement/repository/BaseRepository.java
//package com.bwc.policymanagement.repository;
//
//import com.bwc.policymanagement.entity.BaseEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.repository.NoRepositoryBean;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@NoRepositoryBean
//public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
//    
//    Optional<T> findByIdAndVersion(UUID id, Long version);
//    
//    default T findByIdOrThrow(UUID id) {
//        return findById(id).orElseThrow(() -> 
//            new RuntimeException("Entity not found with id: " + id));
//    }
//}