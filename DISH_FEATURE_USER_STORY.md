# JIRA User Story: PKU Diet Dish Management Feature

## Epic
**PKU Dietary Management System Enhancement**

## Story
**As a** PKU patient or caregiver  
**I want to** create and manage dishes (meals) composed of multiple food products  
**So that** I can plan complete meals while accurately tracking phenylalanine intake and other nutritional values

---

## Story Details

### Story Points: 13
### Priority: High
### Labels: `pku-diet`, `nutrition`, `meal-planning`, `backend`, `spring-boot`

---

## Business Value

### Problem Statement
Currently, PKU patients can only track individual food products, making it difficult to:
- Plan complete meals with multiple ingredients
- Understand the total nutritional impact of combined foods
- Scale recipes up or down while maintaining nutritional accuracy
- Determine appropriate serving sizes to meet specific PHE targets

### Solution Benefits
- **Comprehensive Meal Planning**: Create dishes with multiple ingredients
- **Accurate Nutritional Tracking**: Automatic calculation of total and per-100g values
- **Flexible Serving Sizes**: Scale recipes and calculate proportional nutrition
- **PHE Target Management**: Determine exact serving sizes to meet PHE limits
- **Historical Accuracy**: Preserve nutritional data even if product information changes

---

## Acceptance Criteria

### AC1: Dish Creation
**Given** I am a PKU patient or caregiver  
**When** I create a new dish with multiple food products and their quantities  
**Then** the system should:
- Store the dish with all ingredient details
- Calculate total nutritional values for the entire dish
- Calculate per-100g nutritional values for easy comparison
- Automatically set the nominal serving size as the sum of all ingredients
- Allow me to override the serving size manually if needed

### AC2: Dish Nutritional Display
**Given** I have created a dish  
**When** I view the dish details  
**Then** I should see:
- Dish name, category, and total serving size
- Complete nutritional breakdown (PHE, protein, calories, etc.) for the full serving
- Per-100g nutritional values for comparison with other foods
- Individual ingredient contributions to the total nutrition
- Clear indication if serving size is auto-calculated or manually set

### AC3: Dish Scaling
**Given** I have a dish with known nutritional values  
**When** I specify a target serving size (e.g., 200g instead of 150g)  
**Then** the system should:
- Calculate the scaling factor
- Provide proportionally scaled nutritional values
- Show both original and scaled serving information
- Maintain accuracy to 2 decimal places

### AC4: PHE Target Solving
**Given** I have a dish and want to meet a specific PHE target  
**When** I specify my target PHE amount (e.g., 100mg)  
**Then** the system should:
- Calculate the exact grams of dish needed to achieve that PHE amount
- Show the complete nutritional profile for that serving size
- Optionally consider protein targets as a secondary goal
- Explain if protein targets cannot be exactly met alongside PHE targets

### AC5: Dish Composition Management
**Given** I have an existing dish  
**When** I modify the ingredients (add, remove, or change quantities)  
**Then** the system should:
- Update all nutritional calculations automatically
- Preserve historical data through ingredient snapshots
- Recalculate serving size if in auto mode
- Maintain manual serving size if override is enabled

### AC6: Dish Discovery and Filtering
**Given** I want to find suitable dishes for my dietary needs  
**When** I search for dishes  
**Then** I should be able to:
- Search by dish name
- Filter by category (breakfast, lunch, dinner, snack)
- Find dishes with PHE content below my specified limit
- View paginated results with nutritional summaries

---

## Technical Requirements

### API Endpoints Required
```
POST   /api/v1/dishes                    # Create dish
GET    /api/v1/dishes/{id}               # Get dish details  
GET    /api/v1/dishes                    # List/search dishes
PUT    /api/v1/dishes/{id}/items         # Update ingredients
DELETE /api/v1/dishes/{id}               # Delete dish
POST   /api/v1/dishes/{id}/scale         # Scale to target size
POST   /api/v1/dishes/{id}/solve-mass    # Calculate required grams
GET    /api/v1/dishes/categories         # Get categories
GET    /api/v1/dishes/low-phe           # Find low-PHE dishes
```

### Data Model Requirements
- **Dish Entity**: ID, name, category, serving size, nutritional totals, per-100g values
- **DishItem Entity**: Links dish to products with quantities and historical snapshots
- **Proper Relationships**: Foreign keys with cascade rules
- **Constraints**: Positive values, required fields
- **Indexes**: Performance optimization for queries

### Business Logic Requirements
- **Calculation Accuracy**: All values rounded to 2 decimal places using HALF_UP
- **Historical Preservation**: Product snapshots prevent data loss from product changes
- **Serving Size Logic**: Auto-calculation vs manual override
- **PHE Priority**: Phenylalanine targets take precedence over protein targets

---

## Definition of Done

### Code Quality
- [ ] All endpoints implemented with proper error handling
- [ ] Comprehensive input validation with meaningful error messages
- [ ] Unit tests for calculation logic (>90% coverage)
- [ ] Integration tests for API endpoints
- [ ] Code follows established patterns and conventions

### Documentation
- [ ] OpenAPI/Swagger documentation for all endpoints
- [ ] Database migration scripts with proper indexes
- [ ] README documentation with usage examples
- [ ] Inline code comments for complex business logic

### Database
- [ ] Migration scripts create tables with proper constraints
- [ ] Indexes added for performance optimization
- [ ] Foreign key relationships properly configured
- [ ] Sample data provided for testing

### Testing
- [ ] All acceptance criteria verified through automated tests
- [ ] Edge cases handled (zero values, large numbers, invalid inputs)
- [ ] Performance acceptable for typical usage patterns
- [ ] Error scenarios properly handled and documented

### Integration
- [ ] Seamlessly integrates with existing product management
- [ ] No breaking changes to existing API
- [ ] Backward compatibility maintained
- [ ] Proper transaction handling for data consistency

---

## Non-Functional Requirements

### Performance
- Dish creation should complete within 2 seconds
- Complex calculations (scaling, solving) should complete within 1 second
- API should handle concurrent requests efficiently

### Scalability
- Support dishes with up to 50 ingredients
- Handle 1000+ dishes per user efficiently
- Pagination for large result sets

### Reliability
- Data consistency through proper transaction management
- Graceful error handling with user-friendly messages
- Historical data preservation even during product updates

### Security
- Input validation to prevent injection attacks
- Proper error messages that don't leak system information
- Future-ready for user-based access control

---

## Dependencies

### Prerequisites
- Existing Product entity and ProductRepository
- Database migration infrastructure (Flyway)
- Spring Boot validation framework
- Existing exception handling patterns

### External Dependencies
- No new external libraries required
- Uses existing Spring Boot JPA stack
- Leverages current PostgreSQL database

---

## Risk Assessment

### Technical Risks
- **Complex Calculations**: Mitigation through comprehensive testing and helper classes
- **Performance**: Addressed through proper indexing and query optimization
- **Data Integrity**: Managed through transactions and constraints

### Business Risks
- **User Adoption**: Mitigated through intuitive API design and clear documentation
- **Accuracy Requirements**: Addressed through rigorous calculation testing
- **Historical Data**: Managed through snapshot mechanism

---

## Success Metrics

### Functional Success
- All acceptance criteria pass automated tests
- API responses within performance requirements
- Zero data loss scenarios in testing

### Business Success
- Enables complete meal planning workflows
- Reduces manual calculation errors
- Improves user confidence in dietary management

---

## Out of Scope

### Not Included in This Story
- Frontend UI implementation
- User authentication and authorization
- Recipe import from external sources
- Meal planning calendar features
- Nutritional goal tracking over time
- Recipe sharing between users

### Future Considerations
- Advanced search capabilities
- Bulk operations for multiple dishes
- Recipe optimization algorithms
- Integration with wearable devices
- Mobile app synchronization

---

## Stakeholder Sign-off

**Product Owner**: _[Signature Required]_  
**Tech Lead**: _[Signature Required]_  
**QA Lead**: _[Signature Required]_  

---

**Created**: [Date]  
**Sprint**: [Sprint Number]  
**Assignee**: [Developer Name]  
**Reporter**: [Business Analyst Name]
