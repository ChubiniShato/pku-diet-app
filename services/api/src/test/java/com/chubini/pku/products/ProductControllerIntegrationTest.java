package com.chubini.pku.products;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void testGetProducts() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  public void testCreateProduct() throws Exception {
    // Create a simple product DTO using constructor or setters
    String productJson =
        """
            {
                "productName": "Test Product",
                "category": "Test Category",
                "phenylalanine": 10.5,
                "protein": 5.0,
                "kilocalories": 100.0
            }
            """;

    mockMvc
        .perform(
            post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(productJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productName").value("Test Product"));
  }

  @Test
  public void testUploadValidCsv() throws Exception {
    String csvContent =
        "name,category,phenylalanine,protein,kilocalories\n"
            + "Apple,Fruit,1.0,0.3,52.0\n"
            + "Banana,Fruit,1.2,1.1,89.0";

    MockMultipartFile file =
        new MockMultipartFile("file", "products.csv", "text/csv", csvContent.getBytes());

    mockMvc.perform(multipart("/api/v1/products/upload-csv").file(file)).andExpect(status().isOk());
  }

  @Test
  public void testUploadInvalidFileType() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "products.txt", "application/pdf", "invalid content".getBytes());

    mockMvc
        .perform(multipart("/api/v1/products/upload-csv").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("File validation error")));
  }

  @Test
  public void testGetProductsByCategory() throws Exception {
    mockMvc
        .perform(get("/api/v1/products/category/Fruit").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  public void testGetLowPheProducts() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products/low-phe")
                .param("maxPhe", "5.0")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }
}
