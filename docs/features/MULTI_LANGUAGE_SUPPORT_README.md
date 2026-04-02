# Multi-Language Support for PKU Diet App

This document describes the multi-language support implementation for the PKU Diet App, including Georgian (ka), Russian (ru), and English (en) localization.

## Overview

The multi-language support allows:
- Localized product names and categories
- UTF-8 CSV uploads with multi-language headers
- Automatic fallback to English when translations are missing
- Support for Georgian, Russian, and English languages

## Database Schema

### Product Translations Table

```sql
CREATE TABLE product_translations (
  id BIGSERIAL PRIMARY KEY,
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  locale VARCHAR(8) NOT NULL,           -- 'ka', 'ru', 'en'
  product_name TEXT NOT NULL,
  category TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);
```

### Product Code Field

Products now include a `product_code` field for stable identification during CSV uploads:

```sql
ALTER TABLE products ADD COLUMN product_code VARCHAR(50);
```

## API Endpoints

### Localized Product List

```
GET /api/v1/products?lang=ka&q=ვაშლი&page=0&size=20
```

**Parameters:**
- `lang` (optional): Language code (ka, ru, en)
- `Accept-Language` header (optional): Fallback language preference
- `q` (optional): Search query
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "productCode": "A001",
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

The CSV upload supports multiple language headers:

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
A001,ვაშლი,ხილი
A002,ბანანი,ხილი
A003,ყველი,რძის პროდუქტები
```

**Russian (ru):**
```csv
product_code,name,category
A001,Яблоко,Фрукты
A002,Банан,Фрукты
A003,Сыр,Молочные продукты
```

**English (en):**
```csv
product_code,name,category
A001,Apple,Fruit
A002,Banana,Fruit
A003,Cheese,Dairy
```

## Language Fallback

The system implements a three-tier fallback strategy:

1. **Requested Language**: If translation exists for the requested language (ka/ru/en)
2. **English Fallback**: If requested language translation is missing, fall back to English
3. **Original Data**: If no translations exist, use original product data

## Configuration

### UTF-8 Support

The application is configured for UTF-8 support:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pku?characterEncoding=UTF-8&useUnicode=true
  jpa:
    properties:
      hibernate:
        connection:
          charSet: UTF-8
```

### File Upload

CSV files are processed with UTF-8 encoding:

```java
new InputStreamReader(inputStream, StandardCharsets.UTF_8)
```

## Testing

### Unit Tests

- `MultiLanguageSupportTest`: Tests language normalization and service methods
- `TranslationCsvServiceTest`: Tests CSV parsing with different language headers

### Sample Data

Test CSV files are provided in `src/test/resources/`:
- `sample-translations-ka.csv`: Georgian translations
- `sample-translations-ru.csv`: Russian translations
- `sample-translations-en.csv`: English translations

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
  -F "file=@translations-ka.csv"
```

### 4. Get Available Locales for Product

```bash
curl "http://localhost:8080/api/v1/products/{product-id}/locales"
```

## Error Handling

### CSV Upload Errors

The system provides detailed error reporting:

```json
{
  "locale": "ka",
  "status": "partial",
  "errors": [
    "Unknown product_code: INVALID (line 3)",
    "Missing name for code A002 (line 4)"
  ],
  "message": "Upload completed with 2 errors"
}
```

### Common Error Scenarios

1. **Unknown Product Code**: Product code doesn't exist in database
2. **Missing Required Fields**: Product code or name is missing
3. **Invalid Headers**: CSV headers don't match expected format
4. **File Encoding Issues**: Non-UTF-8 file encoding

## Migration Notes

### Database Migrations

The implementation includes three Flyway migrations:

1. **V22**: Create `product_translations` table
2. **V23**: Add `product_code` field to `products` table
3. **V24**: Backfill English translations from existing data

### Backward Compatibility

- All existing API endpoints continue to work
- Original product data is preserved
- English translations are automatically created from existing data
- No destructive operations are performed

## Performance Considerations

### Database Indexes

The following indexes are created for optimal performance:

```sql
-- Unique constraint for product+locale
CREATE UNIQUE INDEX ux_product_translations_product_locale
  ON product_translations (product_id, locale);

-- Locale-based queries
CREATE INDEX ix_product_translations_locale
  ON product_translations (locale);

-- Case-insensitive name searches
CREATE INDEX ix_product_translations_name_lower
  ON product_translations (LOWER(product_name));
```

### Query Optimization

- Localized queries use LEFT JOINs for efficient fallback
- COALESCE functions provide seamless fallback logic
- Indexes support both exact and case-insensitive searches

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

## Troubleshooting

### Common Issues

1. **CSV Upload Fails**: Check file encoding (must be UTF-8)
2. **Missing Translations**: Verify product codes exist in database
3. **Header Recognition**: Ensure CSV headers match supported aliases
4. **Character Display**: Verify database collation is UTF-8

### Debug Mode

Enable debug logging for translation services:

```yaml
logging:
  level:
    com.chubini.pku.products: DEBUG
```

## Support

For issues or questions regarding multi-language support:

1. Check the test cases for usage examples
2. Review the sample CSV files for proper format
3. Verify database migrations have been applied
4. Check application logs for detailed error messages
