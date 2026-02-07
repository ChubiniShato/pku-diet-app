package com.chubini.pku.products.mapper;

import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductDto;
import com.chubini.pku.products.ProductUpsertDto;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(target = "name", source = "productName")
  ProductDto toDto(Product entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "productNumber", ignore = true)
  @Mapping(target = "productCode", ignore = true)
  @Mapping(target = "translations", ignore = true)
  Product toEntity(ProductUpsertDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "productNumber", ignore = true)
  @Mapping(target = "productCode", ignore = true)
  @Mapping(target = "translations", ignore = true)
  void updateEntityFromDto(ProductUpsertDto dto, @MappingTarget Product entity);
}
