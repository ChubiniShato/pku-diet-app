package com.chubini.pku.products;

/** DTO representing a single row from CSV upload for translations */
public record TranslationUploadRow(String productCode, String name, String category) {}
