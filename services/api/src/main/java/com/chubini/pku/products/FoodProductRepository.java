package com.chubini.pku.api.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodProductRepository extends JpaRepository<FoodProduct, String> {
    Page<FoodProduct> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
