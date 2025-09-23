package com.chubini.pku.products;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MultiLanguageSupportTest {

  @Mock private ProductRepository productRepository;

  @Mock private ProductTranslationRepository translationRepository;

  @Mock private CsvUploadService csvUploadService;

  @Mock private TranslationCsvService translationCsvService;

  @InjectMocks private ProductService productService;

  private Product testProduct;
  private ProductTranslation testTranslation;

  @BeforeEach
  void setUp() {
    testProduct =
        Product.builder()
            .id(UUID.randomUUID())
            .productCode("A001")
            .productName("Apple")
            .category("Fruit")
            .phenylalanine(new BigDecimal("5.0"))
            .leucine(new BigDecimal("10.0"))
            .tyrosine(new BigDecimal("3.0"))
            .methionine(new BigDecimal("2.0"))
            .kilojoules(new BigDecimal("200.0"))
            .kilocalories(new BigDecimal("50.0"))
            .protein(new BigDecimal("1.0"))
            .carbohydrates(new BigDecimal("15.0"))
            .fats(new BigDecimal("0.5"))
            .build();

    testTranslation =
        ProductTranslation.builder()
            .id(1L)
            .product(testProduct)
            .locale("ka")
            .productName("ვაშლი")
            .category("ხილი")
            .build();
  }

  @Test
  void testNormalizeLang_Georgian() {
    // Test Georgian language normalization
    ProductService service =
        new ProductService(
            productRepository, translationRepository, csvUploadService, translationCsvService);

    // Use reflection to access private method for testing
    try {
      var method = ProductService.class.getDeclaredMethod("normalizeLang", String.class);
      method.setAccessible(true);

      assertEquals("ka", method.invoke(service, "ka"));
      assertEquals("ka", method.invoke(service, "ka-GE"));
      assertEquals("ka", method.invoke(service, "Kartuli"));
    } catch (Exception e) {
      fail("Failed to test normalizeLang method: " + e.getMessage());
    }
  }

  @Test
  void testNormalizeLang_Russian() {
    ProductService service =
        new ProductService(
            productRepository, translationRepository, csvUploadService, translationCsvService);

    try {
      var method = ProductService.class.getDeclaredMethod("normalizeLang", String.class);
      method.setAccessible(true);

      assertEquals("ru", method.invoke(service, "ru"));
      assertEquals("ru", method.invoke(service, "ru-RU"));
      assertEquals("ru", method.invoke(service, "Russian"));
    } catch (Exception e) {
      fail("Failed to test normalizeLang method: " + e.getMessage());
    }
  }

  @Test
  void testNormalizeLang_English() {
    ProductService service =
        new ProductService(
            productRepository, translationRepository, csvUploadService, translationCsvService);

    try {
      var method = ProductService.class.getDeclaredMethod("normalizeLang", String.class);
      method.setAccessible(true);

      assertEquals("en", method.invoke(service, "en"));
      assertEquals("en", method.invoke(service, "en-US"));
      assertEquals("en", method.invoke(service, "English"));
      assertEquals("en", method.invoke(service, null));
      assertEquals("en", method.invoke(service, ""));
      assertEquals("en", method.invoke(service, "unknown"));
    } catch (Exception e) {
      fail("Failed to test normalizeLang method: " + e.getMessage());
    }
  }

  @Test
  void testNormalizeLang_Ukrainian() {
    ProductService service =
        new ProductService(
            productRepository, translationRepository, csvUploadService, translationCsvService);

    try {
      var method = ProductService.class.getDeclaredMethod("normalizeLang", String.class);
      method.setAccessible(true);

      assertEquals("uk", method.invoke(service, "uk"));
      assertEquals("uk", method.invoke(service, "uk-UA"));
      assertEquals("uk", method.invoke(service, "Ukrainian"));
    } catch (Exception e) {
      fail("Failed to test normalizeLang method: " + e.getMessage());
    }
  }

  @Test
  void testListLocalized_WithTranslation() {
    // Mock repository to return localized data
    ProductDto expectedDto =
        new ProductDto(
            testProduct.getId(),
            testProduct.getProductCode(),
            "ვაშლი", // Georgian name
            "ხილი", // Georgian category
            testProduct.getPhenylalanine(),
            testProduct.getLeucine(),
            testProduct.getTyrosine(),
            testProduct.getMethionine(),
            testProduct.getKilojoules(),
            testProduct.getKilocalories(),
            testProduct.getProtein(),
            testProduct.getCarbohydrates(),
            testProduct.getFats());

    Page<ProductDto> expectedPage = new PageImpl<>(List.of(expectedDto));
    when(productRepository.findAllLocalized("ka", "ვაშლი", PageRequest.of(0, 20)))
        .thenReturn(expectedPage);

    Page<ProductDto> result = productService.listLocalized("ka", "ვაშლი", 0, 20);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals("ვაშლი", result.getContent().get(0).name());
    assertEquals("ხილი", result.getContent().get(0).category());
  }

  @Test
  void testListLocalized_FallbackToEnglish() {
    // Mock repository to return English fallback
    ProductDto expectedDto =
        new ProductDto(
            testProduct.getId(),
            testProduct.getProductCode(),
            "Apple", // English name (fallback)
            "Fruit", // English category (fallback)
            testProduct.getPhenylalanine(),
            testProduct.getLeucine(),
            testProduct.getTyrosine(),
            testProduct.getMethionine(),
            testProduct.getKilojoules(),
            testProduct.getKilocalories(),
            testProduct.getProtein(),
            testProduct.getCarbohydrates(),
            testProduct.getFats());

    Page<ProductDto> expectedPage = new PageImpl<>(List.of(expectedDto));
    when(productRepository.findAllLocalized("ru", null, PageRequest.of(0, 20)))
        .thenReturn(expectedPage);

    Page<ProductDto> result = productService.listLocalized("ru", null, 0, 20);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals("Apple", result.getContent().get(0).name());
    assertEquals("Fruit", result.getContent().get(0).category());
  }

  @Test
  void testUploadTranslations_Success() throws IOException {
    byte[] csvData = "product_code,name,category\nA001,ვაშლი,ხილი".getBytes();

    when(productRepository.findByProductCode("A001")).thenReturn(Optional.of(testProduct));
    when(translationRepository.findByProductIdAndLocale(testProduct.getId(), "ka"))
        .thenReturn(Optional.empty());
    when(translationRepository.save(any(ProductTranslation.class))).thenReturn(testTranslation);
    when(translationCsvService.importTranslations(eq(csvData), eq("ka"), any(), any()))
        .thenReturn(List.of()); // No errors

    List<String> errors = productService.uploadTranslations("ka", csvData);

    assertTrue(errors.isEmpty());
    verify(translationRepository).save(any(ProductTranslation.class));
  }

  @Test
  void testUploadTranslations_WithErrors() throws IOException {
    byte[] csvData = "product_code,name,category\nINVALID,ვაშლი,ხილი".getBytes();

    when(productRepository.findByProductCode("INVALID")).thenReturn(Optional.empty());
    when(translationCsvService.importTranslations(eq(csvData), eq("ka"), any(), any()))
        .thenReturn(List.of("Unknown product_code: INVALID (line 2)"));

    List<String> errors = productService.uploadTranslations("ka", csvData);

    assertFalse(errors.isEmpty());
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).contains("Unknown product_code"));
  }

  @Test
  void testGetAvailableLocales() {
    when(translationRepository.findByProductId(testProduct.getId()))
        .thenReturn(List.of(testTranslation));

    List<String> locales = productService.getAvailableLocales(testProduct.getId());

    assertNotNull(locales);
    assertEquals(1, locales.size());
    assertEquals("ka", locales.get(0));
  }
}
