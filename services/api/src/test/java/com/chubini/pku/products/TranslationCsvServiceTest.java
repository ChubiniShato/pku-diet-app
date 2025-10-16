package com.chubini.pku.products;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TranslationCsvServiceTest {

  private TranslationCsvService csvService;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    csvService = new TranslationCsvService();

    testProduct =
        Product.builder()
            .id(UUID.randomUUID())
            .productCode("A001")
            .productName("Apple")
            .category("Fruit")
            .build();
  }

  @Test
  void testImportTranslations_EnglishHeaders() throws IOException {
    String csvContent = "product_code,name,category\nA001,Apple,Fruit";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode =
        code -> "A001".equals(code) ? Optional.of(testProduct) : Optional.empty();

    List<String> capturedTranslations = new java.util.ArrayList<>();
    BiConsumer<Product, TranslationUploadRow> upsert =
        (product, row) ->
            capturedTranslations.add(
                product.getProductCode() + ":" + row.name() + ":" + row.category());

    List<String> errors = csvService.importTranslations(csvData, "en", findByCode, upsert);

    assertTrue(errors.isEmpty());
    assertEquals(1, capturedTranslations.size());
    assertEquals("A001:Apple:Fruit", capturedTranslations.get(0));
  }

  @Test
  void testImportTranslations_GeorgianHeaders() throws IOException {
    String csvContent = "პროდუქტის კოდი,პროდუქტის დასახელება,კატეგორია\nA001,ვაშლი,ხილი";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode =
        code -> "A001".equals(code) ? Optional.of(testProduct) : Optional.empty();

    List<String> capturedTranslations = new java.util.ArrayList<>();
    BiConsumer<Product, TranslationUploadRow> upsert =
        (product, row) ->
            capturedTranslations.add(
                product.getProductCode() + ":" + row.name() + ":" + row.category());

    List<String> errors = csvService.importTranslations(csvData, "ka", findByCode, upsert);

    assertTrue(errors.isEmpty());
    assertEquals(1, capturedTranslations.size());
    assertEquals("A001:ვაშლი:ხილი", capturedTranslations.get(0));
  }

  @Test
  void testImportTranslations_RussianHeaders() throws IOException {
    String csvContent = "код продукта,название,категория\nA001,Яблоко,Фрукты";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode =
        code -> "A001".equals(code) ? Optional.of(testProduct) : Optional.empty();

    List<String> capturedTranslations = new java.util.ArrayList<>();
    BiConsumer<Product, TranslationUploadRow> upsert =
        (product, row) ->
            capturedTranslations.add(
                product.getProductCode() + ":" + row.name() + ":" + row.category());

    List<String> errors = csvService.importTranslations(csvData, "ru", findByCode, upsert);

    assertTrue(errors.isEmpty());
    assertEquals(1, capturedTranslations.size());
    assertEquals("A001:Яблоко:Фрукты", capturedTranslations.get(0));
  }

  @Test
  void testImportTranslations_MissingProductCode() throws IOException {
    String csvContent = "product_code,name,category\n,Apple,Fruit";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode = code -> Optional.empty();
    BiConsumer<Product, TranslationUploadRow> upsert = (product, row) -> {};

    List<String> errors = csvService.importTranslations(csvData, "en", findByCode, upsert);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("Missing product_code"));
  }

  @Test
  void testImportTranslations_UnknownProductCode() throws IOException {
    String csvContent = "product_code,name,category\nUNKNOWN,Apple,Fruit";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode = code -> Optional.empty();
    BiConsumer<Product, TranslationUploadRow> upsert = (product, row) -> {};

    List<String> errors = csvService.importTranslations(csvData, "en", findByCode, upsert);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("Unknown product_code"));
  }

  @Test
  void testImportTranslations_MissingName() throws IOException {
    String csvContent = "product_code,name,category\nA001,,Fruit";
    byte[] csvData = csvContent.getBytes();

    Function<String, Optional<Product>> findByCode =
        code -> "A001".equals(code) ? Optional.of(testProduct) : Optional.empty();
    BiConsumer<Product, TranslationUploadRow> upsert = (product, row) -> {};

    List<String> errors = csvService.importTranslations(csvData, "en", findByCode, upsert);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("Missing name"));
  }

  @Test
  void testValidateCsvHeaders_ValidHeaders() throws IOException {
    String csvContent = "product_code,name,category\nA001,Apple,Fruit";
    byte[] csvData = csvContent.getBytes();

    List<String> errors = csvService.validateCsvHeaders(csvData);

    assertTrue(errors.isEmpty());
  }

  @Test
  void testValidateCsvHeaders_InvalidHeaders() throws IOException {
    String csvContent = "invalid,headers,here\nA001,Apple,Fruit";
    byte[] csvData = csvContent.getBytes();

    List<String> errors = csvService.validateCsvHeaders(csvData);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("Invalid CSV headers"));
  }
}
