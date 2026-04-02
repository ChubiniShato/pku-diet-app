# Multi-Language Support Test Plan

## Pre-Migration Verification

1. **Check Current Database State**
   ```sql
   -- Verify products table structure
   \d products
   
   -- Check if product_code column exists
   SELECT column_name, data_type, is_nullable 
   FROM information_schema.columns 
   WHERE table_name = 'products' AND column_name = 'product_code';
   ```

2. **Verify Flyway Schema History**
   ```sql
   SELECT max(version) FROM flyway_schema_history;
   ```

## Migration Testing

### 1. Apply Migrations
```bash
# Run Flyway migrations
mvn flyway:migrate
```

### 2. Verify Migration Results
```sql
-- Check product_code is populated and unique
SELECT COUNT(*) as total_products, 
       COUNT(DISTINCT product_code) as unique_codes,
       COUNT(*) FILTER (WHERE product_code IS NOT NULL) as non_null_codes
FROM products;

-- Verify product_translations table exists
\d product_translations

-- Check English translations were seeded
SELECT COUNT(*) as en_translations 
FROM product_translations 
WHERE locale = 'en';
```

## API Testing

### 1. Test Localized Product List

**English (default):**
```bash
curl "http://localhost:8080/api/v1/products?lang=en&page=0&size=5"
```

**Georgian:**
```bash
curl "http://localhost:8080/api/v1/products?lang=ka&page=0&size=5"
```

**Russian:**
```bash
curl "http://localhost:8080/api/v1/products?lang=ru&page=0&size=5"
```

**Accept-Language Header:**
```bash
curl -H "Accept-Language: ka" "http://localhost:8080/api/v1/products?page=0&size=5"
```

### 2. Test Search Functionality

**Search in Georgian:**
```bash
curl "http://localhost:8080/api/v1/products?lang=ka&q=ვაშლი"
```

**Search in Russian:**
```bash
curl "http://localhost:8080/api/v1/products?lang=ru&q=Яблоко"
```

**Search in English:**
```bash
curl "http://localhost:8080/api/v1/products?lang=en&q=Apple"
```

### 3. Test Translation Upload

**Upload Georgian Translations:**
```bash
curl -X POST "http://localhost:8080/api/v1/products/upload-translations?locale=ka" \
  -F "file=@translations_ka.csv" \
  -H "Content-Type: multipart/form-data"
```

**Upload Russian Translations:**
```bash
curl -X POST "http://localhost:8080/api/v1/products/upload-translations?locale=ru" \
  -F "file=@translations_ru.csv" \
  -H "Content-Type: multipart/form-data"
```

**Expected Response:**
```json
{
  "locale": "ka",
  "status": "ok",
  "errors": [],
  "message": "All translations uploaded successfully"
}
```

### 4. Test Category Filtering

**Georgian Categories:**
```bash
curl "http://localhost:8080/api/v1/products/category/ხილი?lang=ka"
```

**Russian Categories:**
```bash
curl "http://localhost:8080/api/v1/products/category/Фрукты?lang=ru"
```

### 5. Test Low PHE Products

**Localized Low PHE:**
```bash
curl "http://localhost:8080/api/v1/products/low-phe?lang=ka&maxPhe=50"
```

### 6. Test Available Locales

```bash
curl "http://localhost:8080/api/v1/products/{product-id}/locales"
```

**Expected Response:**
```json
["en", "ka", "ru"]
```

## Error Handling Tests

### 1. Invalid Product Code in CSV
Create `invalid_translations.csv`:
```csv
product_code,name,category
INVALID_CODE,ვაშლი,ხილი
```

```bash
curl -X POST "http://localhost:8080/api/v1/products/upload-translations?locale=ka" \
  -F "file=@invalid_translations.csv"
```

**Expected Response:**
```json
{
  "locale": "ka",
  "status": "partial",
  "errors": ["Unknown product_code: INVALID_CODE (line 2)"],
  "message": "Upload completed with 1 errors"
}
```

### 2. Missing Required Fields
Create `missing_fields.csv`:
```csv
product_code,name,category
P000001,,ხილი
P000002,ბანანი,
```

**Expected Response:**
```json
{
  "locale": "ka",
  "status": "partial",
  "errors": [
    "Missing name for code P000001 (line 2)",
    "Missing product_code at line 3"
  ],
  "message": "Upload completed with 2 errors"
}
```

### 3. Invalid CSV Headers
Create `invalid_headers.csv`:
```csv
wrong_header1,wrong_header2,wrong_header3
P000001,ვაშლი,ხილი
```

**Expected Response:**
```json
{
  "locale": "ka",
  "status": "error",
  "errors": ["Invalid CSV headers: Missing required header. Expected one of: [product_code, code, პროდუქტის კოდი, код продукта, productcode]"],
  "message": "Upload error: ..."
}
```

## Backward Compatibility Tests

### 1. Existing API Endpoints
Verify that existing API endpoints still work without breaking changes:

```bash
# Original list endpoint (should return ProductDto with fallback)
curl "http://localhost:8080/api/v1/products"

# Original category endpoint
curl "http://localhost:8080/api/v1/products/category/Fruit"

# Original low PHE endpoint
curl "http://localhost:8080/api/v1/products/low-phe?maxPhe=50"
```

### 2. Response Format Verification
Ensure all endpoints return `ProductDto` with localized fields:
- `name` (localized product name)
- `category` (localized category)
- `productCode` (stable identifier)
- All nutritional fields (unchanged)

## Performance Tests

### 1. Large Dataset Upload
Test with a CSV containing 1000+ translation rows to verify performance.

### 2. Concurrent Uploads
Test multiple simultaneous translation uploads to ensure thread safety.

### 3. Search Performance
Test search performance with large datasets in different languages.

## UTF-8 Encoding Tests

### 1. Georgian Characters
Verify proper handling of Georgian Unicode characters:
- ვაშლი (apple)
- ბანანი (banana)
- ყველი (cheese)

### 2. Russian Characters
Verify proper handling of Russian Unicode characters:
- Яблоко (apple)
- Банан (banana)
- Сыр (cheese)

### 3. Mixed Language Headers
Test CSV with Georgian headers:
```csv
პროდუქტის კოდი,პროდუქტის დასახელება,კატეგორია
P000001,ვაშლი,ხილი
```

Test CSV with Russian headers:
```csv
код продукта,название,категория
P000001,Яблоко,Фрукты
```

## Database Verification

### 1. Data Integrity
```sql
-- Verify all products have product_code
SELECT COUNT(*) FROM products WHERE product_code IS NULL;

-- Verify product_code uniqueness
SELECT product_code, COUNT(*) 
FROM products 
GROUP BY product_code 
HAVING COUNT(*) > 1;

-- Verify translation relationships
SELECT p.product_code, pt.locale, pt.product_name
FROM products p
JOIN product_translations pt ON p.id = pt.product_id
ORDER BY p.product_code, pt.locale;
```

### 2. Index Performance
```sql
-- Test index usage
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM product_translations 
WHERE locale = 'ka' AND LOWER(product_name) LIKE '%ვაშლი%';
```

## Acceptance Criteria Checklist

- [ ] Migrations apply cleanly without errors
- [ ] All products have unique product_code values
- [ ] English translations are backfilled from existing data
- [ ] CSV upload works for all supported languages (ka, ru, en)
- [ ] Localized search works correctly
- [ ] Fallback to English works when translations are missing
- [ ] Existing API endpoints remain backward compatible
- [ ] UTF-8 characters are handled correctly
- [ ] Error handling provides meaningful messages
- [ ] Performance is acceptable for expected load
- [ ] Database constraints are properly enforced
- [ ] All indexes are created and functional

## Troubleshooting

### Common Issues

1. **Migration Fails**: Check Flyway schema history and resolve conflicts
2. **CSV Upload Errors**: Verify file encoding is UTF-8 and headers match expected format
3. **Character Display Issues**: Ensure database collation is UTF-8
4. **Performance Issues**: Check index usage and query execution plans
5. **Missing Translations**: Verify product_code values match between products and CSV

### Debug Commands

```sql
-- Check current collation
SELECT datname, datcollate, datctype FROM pg_database WHERE datname = current_database();

-- Check product_code distribution
SELECT product_code, COUNT(*) FROM products GROUP BY product_code ORDER BY COUNT(*) DESC;

-- Check translation coverage
SELECT locale, COUNT(*) FROM product_translations GROUP BY locale;
```
