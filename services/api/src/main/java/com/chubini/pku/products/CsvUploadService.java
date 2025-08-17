package main.java.com.chubini.pku.products;

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

    public List<FoodProduct> parseCsvFile(MultipartFile file) throws IOException {
        return parseCsvBytes(file.getBytes());
    }
    
    public List<FoodProduct> parseCsvBytes(byte[] csvData) throws IOException {
        List<FoodProduct> products = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData);
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                try {
                    FoodProduct product = FoodProduct.builder()
                            .name(record.get("name"))
                            .unit(record.get("unit"))
                            .proteinPer100g(parseBigDecimal(record.get("proteinPer100g")))
                            .phePer100g(parseBigDecimal(record.get("phePer100g")))
                            .kcalPer100g(parseInteger(record.get("kcalPer100g")))
                            .category(record.get("category"))
                            .isActive(true)
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
