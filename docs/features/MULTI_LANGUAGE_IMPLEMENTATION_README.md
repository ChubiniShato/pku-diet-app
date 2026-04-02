# Multi-Language Product Support Implementation

## Overview

This implementation adds robust multi-language support to the PKU Diet App, enabling localized product names and categories in Georgian (ka), Russian (ru), and English (en) with automatic fallback to English when translations are missing.

## Implementation Summary

### Database Migrations (V25-V27)

**V25__add_product_code_to_products.sql**
- Adds `product_code` field to products table
- Backfills from existing `product_number` or generates unique codes
- Enforces NOT NULL and UNIQUE constraints

**V26__create_product_translations.sql**
- Creates `product_translations` table with (product_id, locale) unique constraint
- Supports ka, ru, en locales
- Includes performance indexes

**V27__seed_en_translations.sql**
- Non-destructive backfill of English translations from existing product data
- Preserves all existing data

### Domain Models

**ProductTranslation Entity**
```java
@Entity
@Table(name = "product_translations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "locale"}))
public class ProductTranslation {
    private Long id;
    private Product product;
    private String locale; // "ka", "ru", "en"
    private String productName;
    private String category;
    private Instant createdAt;
    private Instant updatedAt;
}
```

**Updated Product Entity**
- Added `productCode` field (unique, not null)
- Added `@OneToMany` relationship to translations

**DTOs**
- `ProductDto`: Localized view with fallback logic
- `TranslationUploadRow`: CSV upload data structure

### Repositories

**ProductTranslationRepository**
- `findByProductIdAndLocale()`
- `findAllByCodesAndLocale()`
- Standard CRUD operations

**Updated ProductRepository**
- `findByProductCode()`: Find by stable product code
- `findAllLocalized()`: Localized queries with English fallback
- `findByCategoryLocalized()`: Category filtering with localization
- `findByMaxPhePer100gLocalized()`: PHE filtering with localization

### Services

**TranslationCsvService**
- UTF-8 strict CSV parsing
- Multi-language header support (EN/KA/RU)
- Row-level error reporting
- Header validation

**Updated ProductService**
- `listLocalized()`: Get localized product list
- `uploadTranslations()`: CSV translation upload
- `normalizeLang()`: Language code normalization
- Backward compatible with existing methods

### Controllers

**Updated ProductController**
- All existing endpoints enhanced with localization
- New `/upload-translations` endpoint
- New `/{id}/locales` endpoint
- Backward compatible response format

## API Endpoints

### Localized Product List
```
GET /api/v1/products?lang=ka&q=ვაშლი&page=0&size=20
```

**Parameters:**
- `lang` (optional): Language code (ka, ru, en)
- `Accept-Language` header (optional): Fallback language
- `q` (optional): Search query
- `page`, `size`: Pagination

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "productCode": "P000001",
      "name": "ვაშლი",
      "category": "ხილი",
      "phenylalanine": 5.0,
      "leucine": 10.0,
      "tyrosine": 3.0,
      "methionine": 2.0,
      "kilojoules": 200.0,
      "kilocalories": 50.0,
      "protein": 1.0,
      "carbohydrates": 15.0,
      "fats": 0.5
    }
  ],
  "pageable": { ... },
  "totalElements": 1
}
```

### Upload Translations
```
POST /api/v1/products/upload-translations?locale=ka
Content-Type: multipart/form-data

file: [CSV file]
```

**Response:**
```json
{
  "locale": "ka",
  "status": "ok",
  "errors": [],
  "message": "All translations uploaded successfully"
}
```

### Get Available Locales
```
GET /api/v1/products/{id}/locales
```

**Response:**
```json
["en", "ka", "ru"]
```

## CSV Format

### Supported Headers

**English:**
- `product_code`, `code`, `productcode`
- `name`, `product_name`, `productname`
- `category`, `cat`

**Georgian:**
- `პროდუქტის კოდი` (product code)
- `პროდუქტის დასახელება` (product name)
- `კატეგორია` (category)

**Russian:**
- `код продукта` (product code)
- `название` (name)
- `категория` (category)

### Sample CSV Files

**Georgian (ka):**
```csv
product_code,name,category
P000001,ვაშლი,ხილი
P000002,ბანანი,ხილი
P000003,ყველი,რძის პროდუქტები
```

**Russian (ru):**
```csv
product_code,name,category
P000001,Яблоко,Фрукты
P000002,Банан,Фрукты
P000003,Сыр,Молочные продукты
```

**English (en):**
```csv
product_code,name,category
P000001,Apple,Fruit
P000002,Banana,Fruit
P000003,Cheese,Dairy
```

## Language Fallback Strategy

The system implements a three-tier fallback:

1. **Requested Language**: If translation exists for requested language (ka/ru/en)
2. **English Fallback**: If requested language translation is missing
3. **Original Data**: If no translations exist, use original product data

## Configuration

### UTF-8 Support
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pku?characterEncoding=UTF-8&useUnicode=true
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 20MB
```

### File Upload
```yaml
app:
  upload:
    max-file-size: 20MB
    max-request-size: 20MB
    allowed-mime-types:
      - text/csv
      - application/vnd.ms-excel
      - application/csv
```

## Database Schema

### Products Table
```sql
ALTER TABLE products ADD COLUMN product_code TEXT NOT NULL UNIQUE;
```

### Product Translations Table
```sql
CREATE TABLE product_translations (
  id BIGSERIAL PRIMARY KEY,
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  locale VARCHAR(8) NOT NULL,
  product_name TEXT NOT NULL,
  category TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE (product_id, locale)
);
```

### Indexes
```sql
CREATE UNIQUE INDEX ux_products_code ON products(product_code);
CREATE INDEX ix_translations_locale ON product_translations(locale);
CREATE INDEX ix_translations_product_id ON product_translations(product_id);
CREATE INDEX ix_translations_name_lower ON product_translations(LOWER(product_name));
```

## Usage Examples

### 1. Get Products in Georgian
```bash
curl "http://localhost:8080/api/v1/products?lang=ka"
```

### 2. Search Products in Russian
```bash
curl "http://localhost:8080/api/v1/products?lang=ru&q=Яблоко"
```

### 3. Upload Georgian Translations
```bash
curl -X POST "http://localhost:8080/api/v1/products/upload-translations?locale=ka" \
  -F "file=@translations_ka.csv"
```

### 4. Get Available Locales
```bash
curl "http://localhost:8080/api/v1/products/{product-id}/locales"
```

## Error Handling

### CSV Upload Errors
```json
{
  "locale": "ka",
  "status": "partial",
  "errors": [
    "Unknown product_code: INVALID (line 3)",
    "Missing name for code P000002 (line 4)"
  ],
  "message": "Upload completed with 2 errors"
}
```

### Common Error Scenarios
1. **Unknown Product Code**: Product code doesn't exist in database
2. **Missing Required Fields**: Product code or name is missing
3. **Invalid Headers**: CSV headers don't match expected format
4. **File Encoding Issues**: Non-UTF-8 file encoding

## Testing

### Unit Tests
- `MultiLanguageSupportTest`: Language normalization and service methods
- `TranslationCsvServiceTest`: CSV parsing with different language headers

### Integration Tests
- Database migration verification
- API endpoint testing
- CSV upload testing
- Fallback behavior testing

### Sample Data
Test CSV files are provided in `src/test/resources/`:
- `translations_ka.csv`: Georgian translations
- `translations_ru.csv`: Russian translations
- `translations_en.csv`: English translations

## Migration Notes

### Backward Compatibility
- All existing API endpoints continue to work
- Original product data is preserved
- English translations are automatically created from existing data
- No destructive operations are performed

### Migration Order
1. V25: Add product_code field and backfill
2. V26: Create product_translations table
3. V27: Seed English translations from existing data

## Performance Considerations

### Database Indexes
- Unique constraint on (product_id, locale)
- Index on locale for fast filtering
- Index on product_id for joins
- Case-insensitive index on product_name for searches

### Query Optimization
- Localized queries use LEFT JOINs for efficient fallback
- COALESCE functions provide seamless fallback logic
- Indexes support both exact and case-insensitive searches

## Troubleshooting

### Common Issues
1. **CSV Upload Fails**: Check file encoding (must be UTF-8)
2. **Missing Translations**: Verify product codes exist in database
3. **Header Recognition**: Ensure CSV headers match supported aliases
4. **Character Display**: Verify database collation is UTF-8

### Debug Commands
```sql
-- Check current collation
SELECT datname, datcollate, datctype FROM pg_database WHERE datname = current_database();

-- Check product_code distribution
SELECT product_code, COUNT(*) FROM products GROUP BY product_code ORDER BY COUNT(*) DESC;

-- Check translation coverage
SELECT locale, COUNT(*) FROM product_translations GROUP BY locale;
```

## Future Enhancements

### Planned Features
1. **Fuzzy Text Search**: Integration with PostgreSQL trigram extension
2. **Bulk Translation Management**: Admin interface for managing translations
3. **Translation Validation**: Automated validation of translation completeness
4. **Additional Languages**: Support for more languages as needed

### Extension Points
The system is designed to be easily extensible:
- Add new languages by extending the `normalizeLang` method
- Add new CSV header aliases in `TranslationCsvService`
- Add new localized query methods in `ProductRepository`

## Security Considerations

### Input Validation
- CSV file size limits (20MB)
- MIME type validation
- UTF-8 encoding enforcement
- SQL injection prevention through parameterized queries

### Data Integrity
- Unique constraints prevent duplicate translations
- Foreign key constraints maintain referential integrity
- Transaction boundaries ensure data consistency

## Monitoring and Logging

### Logging Configuration
```yaml
logging:
  level:
    com.chubini.pku.products: DEBUG
```

### Key Metrics
- Translation upload success/failure rates
- Language distribution in requests
- Search performance by language
- CSV parsing error rates

## Support

For issues or questions regarding multi-language support:

1. Check the test cases for usage examples
2. Review the sample CSV files for proper format
3. Verify database migrations have been applied
4. Check application logs for detailed error messages
5. Refer to the test plan for comprehensive validation steps


