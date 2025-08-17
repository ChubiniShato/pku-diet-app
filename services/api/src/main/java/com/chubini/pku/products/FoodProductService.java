package main.java.com.chubini.pku.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FoodProductService {

    private final FoodProductRepository repository;
    private final CsvUploadService csvUploadService;

    public FoodProductService(FoodProductRepository repository, CsvUploadService csvUploadService) {
        this.repository = repository;
        this.csvUploadService = csvUploadService;
    }

    public Page<FoodProduct> getAllProducts(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return repository.findAll(PageRequest.of(page, size));
        }
        return repository.findByNameContainingIgnoreCase(query, PageRequest.of(page, size));
    }

    public FoodProduct getProductById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public FoodProduct createProduct(FoodProductUpsertDto dto) {
        FoodProduct product = FoodProduct.builder()
                .name(dto.name())
                .unit(dto.unit())
                .proteinPer100g(dto.proteinPer100g())
                .phePer100g(dto.phePer100g())
                .kcalPer100g(dto.kcalPer100g())
                .category(dto.category())
                .isActive(dto.isActive() == null ? true : dto.isActive())
                .build();
        
        return repository.save(product);
    }

    public FoodProduct updateProduct(UUID id, FoodProductUpsertDto dto) {
        FoodProduct existingProduct = getProductById(id);
        
        existingProduct.setName(dto.name());
        existingProduct.setUnit(dto.unit());
        existingProduct.setProteinPer100g(dto.proteinPer100g());
        existingProduct.setPhePer100g(dto.phePer100g());
        existingProduct.setKcalPer100g(dto.kcalPer100g());
        existingProduct.setCategory(dto.category());
        
        if (dto.isActive() != null) {
            existingProduct.setIsActive(dto.isActive());
        }
        
        return repository.save(existingProduct);
    }

    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public FoodProduct toggleProductStatus(UUID id) {
        FoodProduct product = getProductById(id);
        product.setIsActive(!product.getIsActive());
        return repository.save(product);
    }

    public List<String> getAllCategories() {
        return repository.findAllActiveCategories();
    }

    public Page<FoodProduct> getProductsByCategory(String category, int page, int size) {
        return repository.findByCategory(category, PageRequest.of(page, size));
    }

    public Page<FoodProduct> getLowPheProducts(Double maxPhe, int page, int size) {
        return repository.findByMaxPhePer100g(maxPhe, PageRequest.of(page, size));
    }

    public Page<FoodProduct> getActiveProducts(int page, int size) {
        return repository.findByIsActive(true, PageRequest.of(page, size));
    }

    public String uploadProductsFromCsv(byte[] csvData) {
        try {
            // Convert byte array to MultipartFile-like structure
            List<FoodProduct> products = csvUploadService.parseCsvBytes(csvData);
            repository.saveAll(products);
            return "Successfully uploaded " + products.size() + " products";
        } catch (Exception e) {
            throw new ProductUploadException("Error uploading CSV: " + e.getMessage());
        }
    }
}
