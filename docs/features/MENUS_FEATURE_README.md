# Menu Management Feature Documentation

## Overview

The PKU Diet App provides comprehensive menu planning and management capabilities specifically designed for Phenylketonuria (PKU) patients. The system supports both automated menu generation using heuristic algorithms and manual menu creation with real-time nutritional validation.

## Key Features

### 🍽️ **Weekly Menu Planning**
- Create structured weekly menus with 7 days
- Automatic nutritional calculation across all meals
- Budget tracking and cost optimization
- Variety scoring to prevent meal repetition

### 📅 **Daily Menu Management**
- Flexible daily menu structure
- Multiple meal slots (breakfast, lunch, dinner, snacks)
- Real-time nutritional validation against patient norms
- Progress tracking towards daily limits

### 🤖 **Automated Menu Generation**
- **Heuristic Algorithm**: Uses genetic algorithms for optimal meal selection
- **Constraint Satisfaction**: Respects PHE, protein, and calorie limits
- **Variety Optimization**: Balances nutritional needs with meal diversity
- **Pantry Integration**: Considers available ingredients
- **Budget Awareness**: Optimizes for cost efficiency

### ✅ **Real-time Validation**
- **PHE Limit Tracking**: Continuous monitoring of phenylalanine intake
- **Protein Balance**: Ensures adequate protein while respecting limits
- **Calorie Management**: Maintains appropriate energy intake
- **Nutrient Optimization**: Balances macro and micronutrients

## API Endpoints

### Weekly Menu Management

#### Create Weekly Menu
```http
POST /api/v1/menus/weeks
Content-Type: application/json

{
  "patientId": "uuid",
  "startDate": "2024-01-01",
  "title": "Weekly Menu",
  "notes": "Special dietary considerations"
}
```

#### Get Patient's Menu Weeks
```http
GET /api/v1/menus/weeks/patient/{patientId}
```

#### Get Specific Menu Week
```http
GET /api/v1/menus/weeks/{weekId}
```

### Daily Menu Management

#### Create Daily Menu
```http
POST /api/v1/menus/days
Content-Type: application/json

{
  "weekId": "uuid",
  "menuDate": "2024-01-01",
  "title": "Monday Menu",
  "notes": "High protein day"
}
```

#### Get Daily Menu
```http
GET /api/v1/menus/days/{dayId}
```

### Meal Slot Management

#### Get Meal Slots for Day
```http
GET /api/v1/menus/days/{dayId}/slots
```

#### Add Menu Entry to Slot
```http
POST /api/v1/menus/slots/{slotId}/entries
Content-Type: application/json

{
  "productId": "uuid",
  "quantity": 100.0,
  "unit": "g",
  "notes": "Fresh ingredients preferred"
}
```

#### Update Consumed Quantity
```http
PATCH /api/v1/menus/entries/{entryId}/consumed
Content-Type: application/json

{
  "consumedQty": 80.0
}
```

### Automated Generation

#### Generate Weekly Menu
```http
POST /api/v1/generator/weekly
Content-Type: application/json

{
  "patientId": "uuid",
  "startDate": "2024-01-01",
  "budget": 100.0,
  "currency": "EUR",
  "usePantry": true,
  "varietyWeight": 0.8,
  "budgetWeight": 0.7,
  "preferenceWeight": 0.9
}
```

#### Generate Daily Menu
```http
POST /api/v1/generator/daily
Content-Type: application/json

{
  "patientId": "uuid",
  "startDate": "2024-01-01",
  "usePantry": true,
  "varietyWeight": 0.8,
  "budgetWeight": 0.7,
  "preferenceWeight": 0.9
}
```

### Validation & Progress Tracking

#### Validate Menu Day
```http
POST /api/v1/validation/menus/days/{dayId}/validate
```

Response includes:
- Daily nutritional totals
- Compliance status (OK/WARN/BREACH)
- Remaining allowances
- Critical factor analysis

## Data Models

### MenuWeek
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "startDate": "2024-01-01",
  "endDate": "2024-01-07",
  "title": "Weekly Menu",
  "status": "ACTIVE",
  "totalCost": 85.50,
  "nutritionalSummary": {
    "totalCalories": 12500,
    "totalProtein": 425.0,
    "totalPhe": 10500,
    "averageVarietyScore": 8.2
  }
}
```

### MenuDay
```json
{
  "id": "uuid",
  "weekId": "uuid",
  "menuDate": "2024-01-01",
  "title": "Monday Menu",
  "status": "ACTIVE",
  "dayTotals": {
    "calories": 1850,
    "protein": 65.0,
    "phe": 1550,
    "carbohydrate": 180.0,
    "fat": 65.0
  },
  "validationStatus": "OK"
}
```

### MenuEntry
```json
{
  "id": "uuid",
  "slotId": "uuid",
  "productId": "uuid",
  "productName": "Low Protein Bread",
  "quantity": 100.0,
  "unit": "g",
  "plannedQty": 100.0,
  "consumedQty": 80.0,
  "nutritionalData": {
    "calories": 245,
    "protein": 8.5,
    "phe": 180,
    "carbohydrate": 45.0,
    "fat": 2.5
  }
}
```

## Algorithm Details

### Menu Generation Process

1. **Patient Profile Analysis**
   - Extract nutritional norms and restrictions
   - Identify allergen constraints
   - Determine budget and pantry availability

2. **Meal Structure Planning**
   - Define meal slots (breakfast, lunch, dinner, 2-3 snacks)
   - Allocate nutritional targets per slot
   - Consider meal timing preferences

3. **Product Selection**
   - Filter products by nutritional constraints
   - Apply variety scoring algorithms
   - Consider cost optimization
   - Check pantry availability

4. **Optimization Loop**
   - Use genetic algorithm for solution space exploration
   - Evaluate solutions against multiple criteria:
     - Nutritional compliance (40%)
     - Variety score (30%)
     - Budget efficiency (20%)
     - Preference matching (10%)
   - Iterate until optimal solution found or timeout

5. **Validation & Refinement**
   - Validate final menu against all constraints
   - Perform nutritional calculations
   - Generate consumption tracking structure

### Variety Scoring Algorithm

The variety algorithm ensures nutritional diversity while maintaining patient safety:

```
VarietyScore = (CategoryDiversity × 0.4) + (NutrientDistribution × 0.4) + (TemporalSpacing × 0.2)

CategoryDiversity = UniqueFoodCategories / TotalPossibleCategories
NutrientDistribution = StandardDeviation(NutrientRatios) / IdealDeviation
TemporalSpacing = AverageHoursBetweenSimilarFoods / 24
```

## Validation Rules

### PHE (Phenylalanine) Limits
- **Critical Breach**: > 120% of daily allowance
- **Warning**: 100-120% of daily allowance
- **Optimal**: 80-100% of daily allowance

### Protein Balance
- **Minimum**: 80% of daily requirement
- **Maximum**: 110% of daily allowance
- **Distribution**: Evenly spread across meals

### Calorie Management
- **Range**: ±10% of prescribed calories
- **Meal Distribution**: Breakfast 20-25%, Lunch 30-35%, Dinner 30-35%, Snacks 10-15%

## Integration Examples

### Frontend Integration

```javascript
// Generate weekly menu
const generateWeeklyMenu = async (patientId, startDate) => {
  const response = await fetch('/api/v1/generator/weekly', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      patientId,
      startDate,
      budget: 100.0,
      usePantry: true
    })
  });

  const result = await response.json();
  if (result.success) {
    displayMenu(result.generatedMenu);
  }
};

// Track consumption
const updateConsumption = async (entryId, consumedQty) => {
  const response = await fetch(`/api/v1/menus/entries/${entryId}/consumed`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ consumedQty })
  });

  const validation = await response.json();
  updateProgressDisplay(validation);
};
```

### Mobile App Integration

```kotlin
// Android/Kotlin example
suspend fun generateDailyMenu(patientId: UUID, date: LocalDate): MenuDay {
    val request = MenuGenerationRequest(
        patientId = patientId,
        startDate = date,
        usePantry = true
    )

    return apiService.generateDailyMenu(request).data
}

suspend fun validateDayConsumption(dayId: UUID): DayValidationResponse {
    return apiService.validateMenuDay(dayId)
}
```

## Performance Considerations

### Caching Strategy
- Menu templates cached for 24 hours
- Product nutritional data cached for 1 hour
- Patient norms cached for session duration

### Database Optimization
- Composite indexes on frequently queried columns
- Partitioning for large menu history tables
- Read replicas for reporting queries

### API Rate Limiting
- 100 requests per minute per client
- Burst allowance for mobile app sync
- Exponential backoff for retries

## Monitoring & Analytics

### Key Metrics
- Menu generation success rate
- Average generation time
- User engagement with generated menus
- Nutritional compliance rates
- Cost optimization achieved

### Health Checks
- Database connectivity
- External service availability (OpenFoodFacts)
- Menu generation pipeline health
- Validation service responsiveness

## Future Enhancements

- **Machine Learning**: Personalized menu recommendations
- **Recipe Integration**: Custom recipe support with PHE calculations
- **Social Features**: Menu sharing and community recipes
- **IoT Integration**: Smart kitchen appliance connectivity
- **Advanced Analytics**: Predictive nutritional modeling
