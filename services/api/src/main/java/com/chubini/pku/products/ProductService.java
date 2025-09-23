package com.chubini.pku.products;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

  private final ProductRepository repository;
  private final ProductTranslationRepository translationRepository;
  private final CsvUploadService csvUploadService;
  private final TranslationCsvService translationCsvService;

  public ProductService(
      ProductRepository repository,
      ProductTranslationRepository translationRepository,
      CsvUploadService csvUploadService,
      TranslationCsvService translationCsvService) {
    this.repository = repository;
    this.translationRepository = translationRepository;
    this.csvUploadService = csvUploadService;
    this.translationCsvService = translationCsvService;
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
    return repository
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
  }

  /** Get localized product by ID with fallback to English */
  @Transactional(readOnly = true)
  public ProductDto getProductByIdLocalized(UUID id, String lang) {
    String normalizedLang = normalizeLang(lang);
    return repository
        .findByIdLocalized(normalizedLang, id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
  }

  public Product createProduct(ProductUpsertDto dto) {
    Product product =
        Product.builder()
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

  /** Get distinct categories localized to provided language with fallback to English */
  @Transactional(readOnly = true)
  public List<String> getAllCategoriesLocalized(String lang) {
    String normalizedLang = normalizeLang(lang);
    return repository.findAllCategoriesLocalized(normalizedLang);
  }

  public Page<Product> getProductsByCategory(String category, int page, int size) {
    return repository.findByCategory(category, PageRequest.of(page, size));
  }

  public Page<Product> getLowPheProducts(Double maxPhe, int page, int size) {
    return repository.findByMaxPhePer100g(maxPhe, PageRequest.of(page, size));
  }

  public String uploadProductsFromCsv(byte[] csvData) {
    try {
      List<Product> products = csvUploadService.parseCsvBytes(csvData);

      int created = 0;
      int updated = 0;

      for (Product incoming : products) {
        // Upsert by product_code to allow re-uploads without failing on uniques
        var existingOpt = repository.findByProductCode(incoming.getProductCode());
        if (existingOpt.isPresent()) {
          Product existing = existingOpt.get();
          existing.setProductName(incoming.getProductName());
          existing.setCategory(incoming.getCategory());
          existing.setPhenylalanine(incoming.getPhenylalanine());
          existing.setLeucine(incoming.getLeucine());
          existing.setTyrosine(incoming.getTyrosine());
          existing.setMethionine(incoming.getMethionine());
          existing.setKilojoules(incoming.getKilojoules());
          existing.setKilocalories(incoming.getKilocalories());
          existing.setProtein(incoming.getProtein());
          existing.setCarbohydrates(incoming.getCarbohydrates());
          existing.setFats(incoming.getFats());
          repository.save(existing);
          updated++;
        } else {
          repository.save(incoming);
          created++;
        }
      }

      return "Upload complete: created=" + created + ", updated=" + updated;
    } catch (Exception e) {
      throw new ProductUploadException("Error uploading CSV: " + e.getMessage());
    }
  }

  // ===== LOCALIZATION METHODS =====

  /** Get localized product list with fallback to English */
  @Transactional(readOnly = true)
  public Page<ProductDto> listLocalized(
      String lang, String query, String category, int page, int size) {
    String normalizedLang = normalizeLang(lang);

    // If category is specified, use category-specific query
    if (category != null && !category.isBlank()) {
      return repository.findByCategoryLocalized(
          normalizedLang, category, query, PageRequest.of(page, size));
    }

    // Otherwise, use general query
    return repository.findAllLocalized(normalizedLang, query, PageRequest.of(page, size));
  }

  /** Get localized products by category with fallback to English */
  @Transactional(readOnly = true)
  public Page<ProductDto> getProductsByCategoryLocalized(
      String lang, String category, int page, int size) {
    String normalizedLang = normalizeLang(lang);
    return repository.findByCategoryLocalized(normalizedLang, category, PageRequest.of(page, size));
  }

  /** Get localized low PHE products with fallback to English */
  @Transactional(readOnly = true)
  public Page<ProductDto> getLowPheProductsLocalized(
      String lang, Double maxPhe, int page, int size) {
    String normalizedLang = normalizeLang(lang);
    return repository.findByMaxPhePer100gLocalized(
        normalizedLang, maxPhe, PageRequest.of(page, size));
  }

  /** Upload translations from CSV */
  @Transactional
  public List<String> uploadTranslations(String locale, byte[] csvBytes) throws IOException {
    String normalizedLocale = normalizeLang(locale);

    return translationCsvService.importTranslations(
        csvBytes,
        normalizedLocale,
        code -> repository.findByProductCode(code),
        (product, row) -> {
          var existing =
              translationRepository
                  .findByProductIdAndLocale(product.getId(), normalizedLocale)
                  .orElseGet(
                      () -> {
                        var translation = new ProductTranslation();
                        translation.setProduct(product);
                        translation.setLocale(normalizedLocale);
                        return translation;
                      });

          existing.setProductName(row.name());
          existing.setCategory(row.category());
          translationRepository.save(existing);
        });
  }

  /** Validate CSV headers for translation upload */
  public List<String> validateTranslationCsv(byte[] csvBytes) throws IOException {
    return translationCsvService.validateCsvHeaders(csvBytes);
  }

  /** Get all available locales for a product */
  @Transactional(readOnly = true)
  public List<String> getAvailableLocales(UUID productId) {
    return translationRepository.findByProductId(productId).stream()
        .map(ProductTranslation::getLocale)
        .toList();
  }

  /** Normalize language code to supported locales */
  private String normalizeLang(String lang) {
    if (lang == null || lang.isBlank()) {
      return "en";
    }

    String normalized = lang.toLowerCase(Locale.ROOT);

    if (normalized.startsWith("ka")) {
      return "ka";
    }
    if (normalized.startsWith("ru")) {
      return "ru";
    }
    if (normalized.startsWith("uk")) {
      return "uk";
    }
    if (normalized.startsWith("en")) {
      return "en";
    }

    // Default to English for unknown languages
    return "en";
  }
}
