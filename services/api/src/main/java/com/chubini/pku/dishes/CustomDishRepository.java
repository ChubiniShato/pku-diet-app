package com.chubini.pku.dishes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomDishRepository extends JpaRepository<CustomDish, UUID> {

    Page<CustomDish> findByPatientIdAndIsVisibleTrue(UUID patientId, Pageable pageable);

    @Query("SELECT cd FROM CustomDish cd WHERE cd.patient.id = :patientId AND cd.isVisible = true AND cd.name ILIKE %:searchTerm%")
    Page<CustomDish> findByPatientIdAndSearchTerm(@Param("patientId") UUID patientId, 
                                                  @Param("searchTerm") String searchTerm, 
                                                  Pageable pageable);

    List<CustomDish> findByPatientIdAndCategory(UUID patientId, String category);

    @Query("SELECT DISTINCT cd.category FROM CustomDish cd WHERE cd.patient.id = :patientId AND cd.isVisible = true AND cd.category IS NOT NULL")
    List<String> findDistinctCategoriesByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT cd FROM CustomDish cd WHERE cd.patient.id = :patientId AND cd.isVisible = true AND cd.per100Phenylalanine <= :maxPhe")
    Page<CustomDish> findLowPheDishes(@Param("patientId") UUID patientId, 
                                     @Param("maxPhe") Double maxPhe, 
                                     Pageable pageable);

    long countByPatientIdAndIsVisibleTrue(UUID patientId);
}


