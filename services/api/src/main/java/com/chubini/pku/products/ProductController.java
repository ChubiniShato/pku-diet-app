package com.chubini.pku.products;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.chubini.pku.validation.FileValidationService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Food Products", description = "CRUD operations for PKU diet food products")
public class ProductController {

  private final ProductService productService;
  private final FileValidationService fileValidationService;

  public ProductController(
      ProductService productService, FileValidationService fileValidationService) {
    this.productService = productService;
    this.fileValidationService = fileValidationService;
  }

  @GetMapping
  @Operation(
      summary = "List food products",
      description = "Get paginated list of food products with optional search")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<ProductDto> list(
      @Parameter(description = "Language code (ka, ru, en)") @RequestParam(required = false)
          String lang,
      @Parameter(description = "Accept-Language header for fallback")
          @RequestHeader(value = "Accept-Language", required = false)
          String acceptLang,
      @Parameter(description = "Search query for product names") @RequestParam(defaultValue = "")
          String query,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    // Use explicit lang parameter or fall back to Accept-Language header
    String language = (lang != null && !lang.isBlank()) ? lang : acceptLang;
    return productService.listLocalized(language, query, page, size);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get food product by ID",
      description = "Retrieve a specific food product by its UUID")
  @ApiResponses(
      value = {
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
  @Operation(
      summary = "Create new food product",
      description = "Create a new food product with nutritional information")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully created product"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<Product> create(@Valid @RequestBody ProductUpsertDto dto) {
    Product product = productService.createProduct(dto);
    return ResponseEntity.ok(product);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update food product", description = "Update an existing food product by ID")
  @ApiResponses(
      value = {
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
  @ApiResponses(
      value = {
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
  @Operation(
      summary = "Get all categories",
      description = "Retrieve list of all product categories")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
  public ResponseEntity<List<String>> getCategories() {
    return ResponseEntity.ok(productService.getAllCategories());
  }

  @GetMapping("/category/{category}")
  @Operation(
      summary = "Get products by category",
      description = "Get paginated list of products in a specific category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<ProductDto> getByCategory(
      @Parameter(description = "Language code (ka, ru, en)") @RequestParam(required = false)
          String lang,
      @Parameter(description = "Accept-Language header for fallback")
          @RequestHeader(value = "Accept-Language", required = false)
          String acceptLang,
      @Parameter(description = "Product category") @PathVariable String category,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    String language = (lang != null && !lang.isBlank()) ? lang : acceptLang;
    return productService.getProductsByCategoryLocalized(language, category, page, size);
  }

  @GetMapping("/low-phe")
  @Operation(
      summary = "Get low PHE products",
      description = "Get products with PHE content below specified maximum")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<ProductDto> getLowPheProducts(
      @Parameter(description = "Language code (ka, ru, en)") @RequestParam(required = false)
          String lang,
      @Parameter(description = "Accept-Language header for fallback")
          @RequestHeader(value = "Accept-Language", required = false)
          String acceptLang,
      @Parameter(description = "Maximum PHE per 100g") @RequestParam Double maxPhe,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    String language = (lang != null && !lang.isBlank()) ? lang : acceptLang;
    return productService.getLowPheProductsLocalized(language, maxPhe, page, size);
  }

  @PostMapping("/upload-csv")
  @Operation(
      summary = "Upload products from CSV",
      description = "Upload multiple products from a CSV file")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded products"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV format or data"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type")
      })
  public ResponseEntity<String> uploadCsv(
      @Parameter(description = "CSV file with product data") @RequestParam("file")
          MultipartFile file) {
    try {
      // Validate file before processing
      fileValidationService.validateFile(file);

      String result = productService.uploadProductsFromCsv(file.getBytes());
      return ResponseEntity.ok(result);
    } catch (ProductUploadException e) {
      return ResponseEntity.badRequest().body("File validation error: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error uploading CSV: " + e.getMessage());
    }
  }

  @PostMapping("/upload-translations")
  @Operation(
      summary = "Upload translations from CSV",
      description = "Upload product translations from a UTF-8 CSV file with multi-language support")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded translations"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV format or data"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type")
      })
  public ResponseEntity<Map<String, Object>> uploadTranslations(
      @Parameter(description = "Language code (ka, ru, en)") @RequestParam String locale,
      @Parameter(description = "CSV file with translation data") @RequestPart("file")
          MultipartFile file) {

    try {
      // Validate file before processing
      fileValidationService.validateFile(file);

      List<String> errors = productService.uploadTranslations(locale, file.getBytes());

      Map<String, Object> response = new HashMap<>();
      response.put("locale", locale);
      response.put("status", errors.isEmpty() ? "ok" : "partial");
      response.put("errors", errors);
      response.put(
          "message",
          errors.isEmpty()
              ? "All translations uploaded successfully"
              : "Upload completed with " + errors.size() + " errors");

      return ResponseEntity.ok(response);

    } catch (IOException e) {
      Map<String, Object> response = new HashMap<>();
      response.put("locale", locale);
      response.put("status", "error");
      response.put("errors", List.of("File processing error: " + e.getMessage()));
      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("locale", locale);
      response.put("status", "error");
      response.put("errors", List.of("Upload error: " + e.getMessage()));
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}/locales")
  @Operation(
      summary = "Get available locales for product",
      description = "Get list of available translation locales for a specific product")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved locales"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  public ResponseEntity<List<String>> getAvailableLocales(
      @Parameter(description = "Product UUID") @PathVariable UUID id) {
    try {
      List<String> locales = productService.getAvailableLocales(id);
      return ResponseEntity.ok(locales);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}
