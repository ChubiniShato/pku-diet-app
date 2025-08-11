package com.chubini.pku.api.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FoodProductRepository extends JpaRepository<FoodProduct, UUID> {
    Page<FoodProduct> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
