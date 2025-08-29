package com.chubini.pku.products;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvUploadService {

    public List<Product> parseCsvFile(MultipartFile file) throws IOException {
        return parseCsvBytes(file.getBytes());
    }
    
    public List<Product> parseCsvBytes(byte[] csvData) throws IOException {
        List<Product> products = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData);
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                try {
                    Product product = Product.builder()
                            .productName(record.get("name"))
                            .category(record.get("category"))
                            .phenylalanine(parseBigDecimal(record.get("phenylalanine")))
                            .leucine(parseBigDecimal(record.get("leucine")))
                            .tyrosine(parseBigDecimal(record.get("tyrosine")))
                            .methionine(parseBigDecimal(record.get("methionine")))
                            .kilojoules(parseBigDecimal(record.get("kilojoules")))
                            .kilocalories(parseBigDecimal(record.get("kilocalories")))
                            .protein(parseBigDecimal(record.get("protein")))
                            .carbohydrates(parseBigDecimal(record.get("carbohydrates")))
                            .fats(parseBigDecimal(record.get("fats")))
                            .build();
                    
                    products.add(product);
                } catch (Exception e) {
                    // Log error and continue with next record
                    System.err.println("Error parsing record: " + record + " - " + e.getMessage());
                }
            }
        }
        
        return products;
    }
    
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
