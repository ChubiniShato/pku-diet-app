package com.chubini.pku.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomProductRepository extends JpaRepository<CustomProduct, UUID> {

    Page<CustomProduct> findByPatientIdAndIsVisibleTrue(UUID patientId, Pageable pageable);

    @Query("SELECT cp FROM CustomProduct cp WHERE cp.patient.id = :patientId AND cp.isVisible = true AND cp.name ILIKE %:searchTerm%")
    Page<CustomProduct> findByPatientIdAndSearchTerm(@Param("patientId") UUID patientId, 
                                                     @Param("searchTerm") String searchTerm, 
                                                     Pageable pageable);

    List<CustomProduct> findByPatientIdAndCategory(UUID patientId, String category);

    @Query("SELECT DISTINCT cp.category FROM CustomProduct cp WHERE cp.patient.id = :patientId AND cp.isVisible = true AND cp.category IS NOT NULL")
    List<String> findDistinctCategoriesByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT cp FROM CustomProduct cp WHERE cp.patient.id = :patientId AND cp.isVisible = true AND cp.phenylalanine <= :maxPhe")
    Page<CustomProduct> findLowPheProducts(@Param("patientId") UUID patientId, 
                                          @Param("maxPhe") Double maxPhe, 
                                          Pageable pageable);

    long countByPatientIdAndIsVisibleTrue(UUID patientId);
}


