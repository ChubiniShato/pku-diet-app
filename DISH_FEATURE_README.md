# PKU Diet App - Dish Feature Implementation

## Overview

The Dish feature allows PKU patients and caregivers to create, manage, and analyze meals composed of multiple food products. This feature provides comprehensive nutritional analysis with automatic calculations for both total values and per-100g normalized values.

## Key Features

### 🍽️ **Dish Management**
- Create dishes with multiple product ingredients
- Automatic nutritional calculation based on ingredient quantities
- Manual serving size override option
- Historical data preservation through product snapshots

### 📊 **Nutritional Analysis**
- **Total Values**: Complete nutritional profile for the entire dish serving
- **Per 100g Values**: Normalized values for easy comparison with other foods
- **Scaling**: Calculate nutritional values for different serving sizes
- **Target Solving**: Determine required dish quantity to meet specific nutritional targets

### 🎯 **PKU-Specific Features**
- **Phenylalanine Priority**: Primary focus on PHE content management
- **Low-PHE Discovery**: Find dishes with PHE content below specified limits
- **Protein Balancing**: Secondary protein target optimization
- **Safe Meal Planning**: Comprehensive nutritional visibility for informed decisions

## API Endpoints

### Core CRUD Operations
```
POST   /api/v1/dishes                    # Create new dish
GET    /api/v1/dishes/{id}               # Get dish details
GET    /api/v1/dishes                    # List dishes (with search/filter)
PUT    /api/v1/dishes/{id}/items         # Update dish composition
DELETE /api/v1/dishes/{id}               # Delete dish
```

### Nutritional Calculations
```
POST   /api/v1/dishes/{id}/scale         # Scale dish to target grams
POST   /api/v1/dishes/{id}/solve-mass    # Calculate required grams for target nutrients
```

### Discovery & Filtering
```
GET    /api/v1/dishes/categories         # Get all dish categories
GET    /api/v1/dishes/low-phe           # Find low-PHE dishes
```

## Database Schema

### Dishes Table
- **Core Info**: ID, name, category, serving size
- **Configuration**: Manual serving override flag
- **Total Nutrition**: Complete nutritional profile for nominal serving
- **Per 100g Nutrition**: Normalized values for comparison
- **Timestamps**: Creation and update tracking

### Dish Items Table
- **Relationships**: Links to dish and product
- **Quantity**: Grams of each ingredient
- **Snapshots**: Historical product nutritional data
- **Audit Trail**: Creation and update timestamps

## Business Logic

### Calculation Rules

1. **Item Contribution**: `(grams × product_per100_value) ÷ 100`
2. **Dish Totals**: Sum of all item contributions
3. **Per 100g Values**: `(total_value × 100) ÷ nominal_serving_grams`
4. **Scaling**: `scaled_value = original_value × scale_factor`

### Serving Size Logic

- **Auto Mode** (default): `nominal_serving_grams = sum(all_item_grams)`
- **Manual Mode**: User-defined serving size, independent of ingredient changes
- **Override Toggle**: Seamless switching between modes

### Target Solving

1. **Primary**: Phenylalanine target (always honored)
2. **Secondary**: Protein target (best effort, if specified)
3. **Calculation**: `required_grams = (target_phe × 100) ÷ dish_per100_phe`

## Usage Examples

### Creating a Dish
```json
POST /api/v1/dishes
{
  "name": "PKU-Safe Breakfast Bowl",
  "category": "Breakfast",
  "items": [
    {"productId": "uuid-1", "grams": 100.00},
    {"productId": "uuid-2", "grams": 50.00}
  ]
}
```

### Scaling a Dish
```json
POST /api/v1/dishes/{id}/scale
{
  "targetGrams": 300.00
}
```

### Solving for Target Nutrients
```json
POST /api/v1/dishes/{id}/solve-mass
{
  "targetPhenylalanine": 150.00,
  "targetProtein": 5.00
}
```

## Implementation Architecture

### Layer Structure
- **Controller**: REST API endpoints with OpenAPI documentation
- **Service**: Business logic and orchestration
- **Repository**: Data access with optimized queries
- **Calculator**: Pure calculation logic (testable, reusable)
- **DTOs**: Request/response data transfer objects

### Key Classes
- `Dish` & `DishItem`: JPA entities with proper relationships
- `DishService`: Core business logic implementation
- `DishCalculator`: Mathematical calculations and transformations
- `DishController`: REST API with comprehensive error handling

### Data Integrity
- **Product Snapshots**: Historical accuracy preserved
- **Cascade Operations**: Proper parent-child relationship management
- **Validation**: Input validation with meaningful error messages
- **Transactions**: ACID compliance for data consistency

## Migration Scripts

- **V4**: Create dishes and dish_items tables with indexes and constraints
- **V5**: Insert sample dishes for demonstration and testing

## Testing Considerations

### Unit Tests
- Calculator logic with edge cases
- Service layer business rules
- DTO validation scenarios

### Integration Tests
- API endpoint functionality
- Database operations
- Error handling flows

### Performance Tests
- Large dish calculations
- Complex queries with joins
- Concurrent dish operations

## Future Enhancements

### Planned Features
- **Recipe Import**: Import from popular recipe formats
- **Meal Planning**: Weekly/monthly meal planning tools
- **Nutritional Goals**: Personal targets and progress tracking
- **Recipe Sharing**: Community recipe exchange

### Optimization Opportunities
- **Caching**: Frequently accessed dish calculations
- **Batch Operations**: Multiple dish operations
- **Search Enhancement**: Full-text search capabilities
- **Analytics**: Usage patterns and popular dishes

## Security Considerations

- **Input Validation**: Comprehensive validation on all inputs
- **SQL Injection**: Parameterized queries and JPA protection
- **Data Sanitization**: Clean user inputs before processing
- **Access Control**: Future user-based dish ownership

## Monitoring & Observability

### Metrics to Track
- Dish creation/modification frequency
- Calculation performance
- Error rates and types
- Popular dish categories

### Logging
- Business operation audit trail
- Performance bottleneck identification
- Error diagnosis information
- User behavior insights

---

## Getting Started

1. **Database Migration**: Run migrations V4 and V5
2. **API Testing**: Use Swagger UI at `/swagger-ui.html`
3. **Sample Data**: Explore pre-loaded sample dishes
4. **Integration**: Connect with existing product management

The Dish feature seamlessly integrates with your existing PKU Diet App infrastructure, providing powerful meal planning capabilities while maintaining the focus on phenylalanine management critical for PKU patients.
