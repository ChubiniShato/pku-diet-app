package com.chubini.pku.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final CsvUploadService csvUploadService;

    public ProductService(ProductRepository repository, CsvUploadService csvUploadService) {
        this.repository = repository;
        this.csvUploadService = csvUploadService;
    }

    // ... existing code ...
    public Page<Product> getAllProducts(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return repository.findAll(PageRequest.of(page, size));
        }
        // Fix: use the correct method name
        return repository.findByProductNameContainingIgnoreCase(query, PageRequest.of(page, size));
    }
// ... existing code ...

    public Product getProductById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(ProductUpsertDto dto) {
        Product product = Product.builder()
                .productName(dto.productName())
                .category(dto.category())
                .phenylalanine(dto.phenylalanine())
                .leucine(dto.leucine())
                .tyrosine(dto.tyrosine())
                .methionine(dto.methionine())
                .kilojoules(dto.kilojoules())
                .kilocalories(dto.kilocalories())
                .protein(dto.protein())
                .carbohydrates(dto.carbohydrates())
                .fats(dto.fats())
                .build();
        
        return repository.save(product);
    }

    public Product updateProduct(UUID id, ProductUpsertDto dto) {
        Product existingProduct = getProductById(id);
        
        // Partial update - only update non-null fields
        if (dto.productName() != null) {
            existingProduct.setProductName(dto.productName());
        }
        if (dto.category() != null) {
            existingProduct.setCategory(dto.category());
        }
        if (dto.phenylalanine() != null) {
            existingProduct.setPhenylalanine(dto.phenylalanine());
        }
        if (dto.leucine() != null) {
            existingProduct.setLeucine(dto.leucine());
        }
        if (dto.tyrosine() != null) {
            existingProduct.setTyrosine(dto.tyrosine());
        }
        if (dto.methionine() != null) {
            existingProduct.setMethionine(dto.methionine());
        }
        if (dto.kilojoules() != null) {
            existingProduct.setKilojoules(dto.kilojoules());
        }
        if (dto.kilocalories() != null) {
            existingProduct.setKilocalories(dto.kilocalories());
        }
        if (dto.protein() != null) {
            existingProduct.setProtein(dto.protein());
        }
        if (dto.carbohydrates() != null) {
            existingProduct.setCarbohydrates(dto.carbohydrates());
        }
        if (dto.fats() != null) {
            existingProduct.setFats(dto.fats());
        }
        
        return repository.save(existingProduct);
    }

    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public List<String> getAllCategories() {
        return repository.findAllCategories();
    }

    public Page<Product> getProductsByCategory(String category, int page, int size) {
        return repository.findByCategory(category, PageRequest.of(page, size));
    }

    public Page<Product> getLowPheProducts(Double maxPhe, int page, int size) {
        return repository.findByMaxPhePer100g(maxPhe, PageRequest.of(page, size));
    }

    public String uploadProductsFromCsv(byte[] csvData) {
        try {
            // Convert byte array to MultipartFile-like structure
            List<Product> products = csvUploadService.parseCsvBytes(csvData);
            repository.saveAll(products);
            return "Successfully uploaded " + products.size() + " products";
        } catch (Exception e) {
            throw new ProductUploadException("Error uploading CSV: " + e.getMessage());
        }
    }
}
