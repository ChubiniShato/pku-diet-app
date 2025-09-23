package com.chubini.pku.products;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

/**
 * Service for parsing UTF-8 CSV files with multi-language header support Supports Georgian,
 * Russian, and English headers
 */
@Service
public class TranslationCsvService {

  /** Headers mapping for different languages */
  record Headers(String code, String name, String category) {}

  /** Resolve headers from CSV, supporting multiple languages */
  private Headers resolveHeaders(Map<String, Integer> headerMap) {
    String code =
        pick(
            headerMap,
            "product_code",
            "code",
            "პროდუქტის კოდი",
            "код продукта",
            "код товару",
            "productcode");
    String name =
        pick(
            headerMap,
            "name",
            "product_name",
            "პროდუქტის დასახელება",
            "название",
            "назва",
            "productname");
    String category = pick(headerMap, "category", "კატეგორია", "категория", "категорія", "cat");

    return new Headers(code, name, category);
  }

  /** Pick the first matching header from aliases */
  private String pick(Map<String, Integer> headerMap, String... aliases) {
    for (String alias : aliases) {
      for (String header : headerMap.keySet()) {
        if (header != null && header.trim().equalsIgnoreCase(alias)) {
          return header;
        }
      }
    }
    throw new IllegalArgumentException(
        "Missing required header. Expected one of: " + Arrays.toString(aliases));
  }

  /**
   * Import translations from CSV with UTF-8 encoding
   *
   * @param csvBytes CSV file bytes
   * @param locale Target locale (ka, ru, en)
   * @param findByCode Function to find product by code
   * @param upsert Function to upsert translation
   * @return List of error messages
   * @throws IOException if CSV parsing fails
   */
  public List<String> importTranslations(
      byte[] csvBytes,
      String locale,
      Function<String, java.util.Optional<Product>> findByCode,
      BiConsumer<Product, TranslationUploadRow> upsert)
      throws IOException {

    List<String> errors = new ArrayList<>();

    try (var inputStream = new ByteArrayInputStream(csvBytes);
        var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        var parser =
            new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

      var headerMap = parser.getHeaderMap();
      var headers = resolveHeaders(headerMap);

      for (CSVRecord record : parser) {
        try {
          String code = record.get(headers.code);
          String name = record.get(headers.name);
          String category = record.isSet(headers.category) ? record.get(headers.category) : null;

          // Validate required fields
          if (code == null || code.isBlank()) {
            errors.add("Missing product_code at line " + record.getRecordNumber());
            continue;
          }

          if (name == null || name.isBlank()) {
            errors.add(
                "Missing name for code " + code + " (line " + record.getRecordNumber() + ")");
            continue;
          }

          // Find product by code
          var product = findByCode.apply(code.trim());
          if (product.isEmpty()) {
            errors.add(
                "Unknown product_code: " + code + " (line " + record.getRecordNumber() + ")");
            continue;
          }

          // Upsert translation
          upsert.accept(
              product.get(), new TranslationUploadRow(code.trim(), name.trim(), category));

        } catch (Exception e) {
          errors.add("Error processing line " + record.getRecordNumber() + ": " + e.getMessage());
        }
      }
    }

    return errors;
  }

  /** Validate CSV file format and headers */
  public List<String> validateCsvHeaders(byte[] csvBytes) throws IOException {
    List<String> errors = new ArrayList<>();

    try (var inputStream = new ByteArrayInputStream(csvBytes);
        var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        var parser =
            new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

      var headerMap = parser.getHeaderMap();

      try {
        resolveHeaders(headerMap);
      } catch (IllegalArgumentException e) {
        errors.add("Invalid CSV headers: " + e.getMessage());
      }

    } catch (Exception e) {
      errors.add("Failed to parse CSV: " + e.getMessage());
    }

    return errors;
  }
}
