package com.chubini.pku.products.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductDto;
import com.chubini.pku.products.ProductUpsertDto;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {

  private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

  @Test
  void toDto_ShouldMapCorrectly() {
    Product entity =
        Product.builder()
            .productName("Apple")
            .category("Fruit")
            .phenylalanine(new BigDecimal("10.5"))
            .build();

    ProductDto dto = mapper.toDto(entity);

    assertNotNull(dto);
    assertEquals("Apple", dto.name());
    assertEquals("Fruit", dto.category());
    assertEquals(new BigDecimal("10.5"), dto.phenylalanine());
  }

  @Test
  void toEntity_ShouldMapCorrectly() {
    ProductUpsertDto dto =
        new ProductUpsertDto(
            "Banana",
            "Fruit",
            new BigDecimal("1.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"),
            new BigDecimal("0.0"));

    Product entity = mapper.toEntity(dto);

    assertNotNull(entity);
    assertEquals("Banana", entity.getProductName());
    assertEquals("Fruit", entity.getCategory());
    assertEquals(new BigDecimal("1.0"), entity.getPhenylalanine());
  }
}
