package com.chubini.pku.api.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class FoodProductController {

    private final FoodProductRepository repo;

    public FoodProductController(FoodProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Page<FoodProduct> list(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (query == null || query.isBlank()) {
            return repo.findAll(PageRequest.of(page, size));
        }
        return repo.findByNameContainingIgnoreCase(query, PageRequest.of(page, size));
    }

    @PostMapping
    public ResponseEntity<FoodProduct> create(@Valid @RequestBody FoodProductUpsertDto dto) {
        FoodProduct fp = FoodProduct.builder()
                .name(dto.name())
                .unit(dto.unit())
                .proteinPer100g(dto.proteinPer100g())
                .phePer100g(dto.phePer100g())
                .kcalPer100g(dto.kcalPer100g())
                .category(dto.category())
                .isActive(dto.isActive() == null ? true : dto.isActive())
                .build();
        return ResponseEntity.ok(repo.save(fp));
    }
}
