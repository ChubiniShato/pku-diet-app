package com.chubini.pku.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Food Products", description = "CRUD operations for PKU diet food products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List food products", description = "Get paginated list of food products with optional search")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Page<Product> list(
            @Parameter(description = "Search query for product names") @RequestParam(defaultValue = "") String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return productService.getAllProducts(query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get food product by ID", description = "Retrieve a specific food product by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getById(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new food product", description = "Create a new food product with nutritional information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created product"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Product> create(@Valid @RequestBody ProductUpsertDto dto) {
        Product product = productService.createProduct(dto);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update food product", description = "Update an existing food product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated product"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Product> update(
            @Parameter(description = "Product UUID") @PathVariable UUID id, 
            @Valid @RequestBody ProductUpsertDto dto) {
        try {
            Product product = productService.updateProduct(id, dto);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete food product", description = "Delete a food product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted product"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve list of all product categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Get paginated list of products in a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Page<Product> getByCategory(
            @Parameter(description = "Product category") @PathVariable String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return productService.getProductsByCategory(category, page, size);
    }

    @GetMapping("/low-phe")
    @Operation(summary = "Get low PHE products", description = "Get products with PHE content below specified maximum")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Page<Product> getLowPheProducts(
            @Parameter(description = "Maximum PHE per 100g") @RequestParam Double maxPhe,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return productService.getLowPheProducts(maxPhe, page, size);
    }

    @PostMapping("/upload-csv")
    @Operation(summary = "Upload products from CSV", description = "Upload multiple products from a CSV file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded products"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV format or data")
    })
    public ResponseEntity<String> uploadCsv(
            @Parameter(description = "CSV file with product data") @RequestParam("file") MultipartFile file) {
        try {
            String result = productService.uploadProductsFromCsv(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading CSV: " + e.getMessage());
        }
    }
}
