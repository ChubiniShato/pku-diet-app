package com.chubini.pku.products;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.chubini.pku.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@Tag("it")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisabledIfSystemProperty(
    named = "skipDockerTests",
    matches = "true",
    disabledReason = "Docker/Testcontainers not available")
class MultiLanguageIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ProductRepository productRepository;

  @Autowired private ProductTranslationRepository translationRepository;

  @Autowired private ObjectMapper objectMapper;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    // Create a test product
    testProduct =
        Product.builder()
            .productCode("P000001")
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

    testProduct = productRepository.save(testProduct);

    // Create English translation
    ProductTranslation enTranslation =
        ProductTranslation.builder()
            .product(testProduct)
            .locale("en")
            .productName("Apple")
            .category("Fruit")
            .build();
    translationRepository.save(enTranslation);

    // Create Georgian translation
    ProductTranslation kaTranslation =
        ProductTranslation.builder()
            .product(testProduct)
            .locale("ka")
            .productName("ვაშლი")
            .category("ხილი")
            .build();
    translationRepository.save(kaTranslation);

    // Create Russian translation
    ProductTranslation ruTranslation =
        ProductTranslation.builder()
            .product(testProduct)
            .locale("ru")
            .productName("Яблоко")
            .category("Фрукты")
            .build();
    translationRepository.save(ruTranslation);
  }

  @Test
  void testGetProductsInEnglish() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("lang", "en").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Apple"))
        .andExpect(jsonPath("$.content[0].category").value("Fruit"))
        .andExpect(jsonPath("$.content[0].productCode").value("P000001"));
  }

  @Test
  void testGetProductsInGeorgian() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("lang", "ka").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("ვაშლი"))
        .andExpect(jsonPath("$.content[0].category").value("ხილი"))
        .andExpect(jsonPath("$.content[0].productCode").value("P000001"));
  }

  @Test
  void testGetProductsInRussian() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("lang", "ru").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Яблоко"))
        .andExpect(jsonPath("$.content[0].category").value("Фрукты"))
        .andExpect(jsonPath("$.content[0].productCode").value("P000001"));
  }

  @Test
  void testSearchInGeorgian() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products")
                .param("lang", "ka")
                .param("q", "ვაშლი")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("ვაშლი"))
        .andExpect(jsonPath("$.content[0].category").value("ხილი"));
  }

  @Test
  void testSearchInRussian() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products")
                .param("lang", "ru")
                .param("q", "Яблоко")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Яблоко"))
        .andExpect(jsonPath("$.content[0].category").value("Фрукты"));
  }

  @Test
  void testFallbackToEnglish() throws Exception {
    // Create a product without Georgian translation
    Product productWithoutKa =
        Product.builder()
            .productCode("P000002")
            .productName("Banana")
            .category("Fruit")
            .phenylalanine(new BigDecimal("3.0"))
            .leucine(new BigDecimal("8.0"))
            .tyrosine(new BigDecimal("2.0"))
            .methionine(new BigDecimal("1.0"))
            .kilojoules(new BigDecimal("150.0"))
            .kilocalories(new BigDecimal("35.0"))
            .protein(new BigDecimal("0.8"))
            .carbohydrates(new BigDecimal("8.0"))
            .fats(new BigDecimal("0.2"))
            .build();
    productRepository.save(productWithoutKa);

    // Create only English translation
    ProductTranslation enOnly =
        ProductTranslation.builder()
            .product(productWithoutKa)
            .locale("en")
            .productName("Banana")
            .category("Fruit")
            .build();
    translationRepository.save(enOnly);

    // Request Georgian but should fallback to English
    mockMvc
        .perform(
            get("/api/v1/products")
                .param("lang", "ka")
                .param("q", "Banana")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].name", hasItem("Banana")))
        .andExpect(jsonPath("$.content[*].category", hasItem("Fruit")));
  }

  @Test
  void testGetAvailableLocales() throws Exception {
    mockMvc
        .perform(get("/api/v1/products/{id}/locales", testProduct.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").value("en"))
        .andExpect(jsonPath("$[1]").value("ka"))
        .andExpect(jsonPath("$[2]").value("ru"));
  }

  @Test
  void testUploadTranslationsSuccess() throws Exception {
    String csvContent = "product_code,name,category\nP000001,ვაშლი,ხილი";

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "translations.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart("/api/v1/products/upload-translations")
                .file(file)
                .param("locale", "ka")
                .header("Idempotency-Key", "test-translations-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.locale").value("ka"))
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  void testUploadTranslationsWithErrors() throws Exception {
    String csvContent = "product_code,name,category\nINVALID,ვაშლი,ხილი\nP000001,,ხილი";

    MockMultipartFile fileWithErrors =
        new MockMultipartFile(
            "file", "translations.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart("/api/v1/products/upload-translations")
                .file(fileWithErrors)
                .param("locale", "ka")
                .header("Idempotency-Key", "test-translations-456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.locale").value("ka"))
        .andExpect(jsonPath("$.status").value("partial"))
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors.length()").value(2));
  }

  @Test
  void testCategoryFilteringInGeorgian() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products/category/ხილი")
                .param("lang", "ka")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("ვაშლი"))
        .andExpect(jsonPath("$.content[0].category").value("ხილი"));
  }

  @Test
  void testCategoryFilteringInRussian() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products/category/Фрукты")
                .param("lang", "ru")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Яблоко"))
        .andExpect(jsonPath("$.content[0].category").value("Фрукты"));
  }

  @Test
  void testLowPheProductsLocalized() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products/low-phe")
                .param("lang", "ka")
                .param("maxPhe", "10.0")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("ვაშლი"))
        .andExpect(jsonPath("$.content[0].category").value("ხილი"));
  }

  @Test
  void testAcceptLanguageHeader() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products")
                .header("Accept-Language", "ka")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("ვაშლი"))
        .andExpect(jsonPath("$.content[0].category").value("ხილი"));
  }

  @Test
  void testBackwardCompatibility() throws Exception {
    // Test that existing endpoints still work without lang parameter
    mockMvc
        .perform(get("/api/v1/products").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Apple"))
        .andExpect(jsonPath("$.content[0].category").value("Fruit"));
  }
}
