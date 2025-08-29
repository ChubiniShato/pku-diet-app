# PKU Diet App

A comprehensive web application to support dietary management for **Phenylketonuria (PKU)** patients. The backend provides complete menu planning, nutritional tracking, patient management, and food product APIs with robust security and validation.

---

## ✨ Features

### 🏥 **Patient Management**
- Patient profile creation and management
- Norm prescriptions (PHE, protein, calorie limits)
- Allergen tracking and dietary restrictions
- Activity level and demographic data

### 🍽️ **Menu Planning**
- Weekly and daily menu generation
- Meal slot management (breakfast, lunch, dinner, snacks)
- Nutritional calculation and validation
- Menu sharing capabilities

### 🥗 **Food & Nutrition**
- Comprehensive food product database
- Custom dish creation with ingredients
- Nutritional analysis (PHE, protein, calories, etc.)
- CSV import for bulk product data

### 🔒 **Security & Validation**
- Spring Security with configurable authentication
- File upload validation and security
- Rate limiting protection
- CORS configuration for frontend integration

### 📊 **Monitoring & Health**
- Health check endpoints
- Database connectivity monitoring
- Comprehensive error handling
- Swagger/OpenAPI documentation

---

## 🛠 Tech Stack
- **Java 21** with Spring Boot 3.3.2
- **PostgreSQL** with Flyway migrations
- **Maven** for dependency management
- **Docker** with health checks
- **MapStruct** for DTO mapping
- **Swagger/OpenAPI** for API documentation
- **JUnit 5** for testing

---

## 🚀 Getting Started

### Prerequisites
- Java 21  
- Maven 3.6+  
- Docker & Docker Compose (recommended)
- PostgreSQL 16 (if running locally)

### Quick Start with Docker (Recommended)
```bash
# Clone the repository
git clone https://github.com/ChubiniShato/pku-diet-app.git
cd pku-diet-app

# Copy environment template
cp .env.example .env
# Edit .env with your configuration

# Start the application
docker-compose up -d

# Check health
curl http://localhost:8080/actuator/health
```

### Local Development
```bash
cd services/api

# Build the application
mvn clean compile

# Run with Maven
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/api-0.0.1-SNAPSHOT.jar
```

### Running Tests
```bash
cd services/api
mvn test
```

---

## 📡 API Endpoints

### 🏥 **Patient Management**
- `GET /api/v1/patients` → List patients
- `POST /api/v1/patients` → Create patient
- `GET /api/v1/patients/{id}` → Get patient details
- `PUT /api/v1/patients/{id}` → Update patient

### 🍽️ **Menu Management**
- `GET /api/v1/menus/weeks/patient/{id}` → Get patient's menu weeks
- `POST /api/v1/menus/weeks` → Create menu week
- `GET /api/v1/menus/days/{id}` → Get daily menu
- `POST /api/v1/menus/slots/{id}/entries` → Add food to meal

### 🥗 **Product Management**
- `GET /api/v1/products` → List products (paginated)
- `POST /api/v1/products` → Create product
- `POST /api/v1/products/upload-csv` → Bulk upload via CSV
- `GET /api/v1/products/low-phe?maxPhe=5.0` → Find low-PHE products

### 📊 **Validation & Generation**
- `POST /api/v1/validation/norms` → Validate nutritional norms
- `POST /api/v1/generation/menu` → Generate optimized menu

### 🔧 **Health & Monitoring**
- `GET /actuator/health` → Application health
- `GET /swagger-ui.html` → API documentation

---

## 🐳 Docker Configuration

The application includes:
- **Health checks** for both database and API
- **Environment variable** configuration with defaults
- **Multi-stage builds** for optimized images
- **Resource limits** and security best practices

---

## 🔒 Security Features

- **Authentication**: HTTP Basic (configurable to JWT)
- **File Upload**: Size limits, MIME type validation, path traversal protection
- **Rate Limiting**: 100 requests per minute per client
- **CORS**: Configurable for frontend integration
- **Input Validation**: Comprehensive validation on all endpoints

---

## 🧪 Testing

The application includes:
- **Integration tests** for all major endpoints
- **H2 in-memory database** for testing
- **MockMvc** for web layer testing
- **Test profiles** with separate configuration

---

## 📋 Environment Configuration

Create a `.env` file (copy from `.env.example`):
```bash
# Database
DB_NAME=pku
DB_USER=pku
DB_PASSWORD=your-secure-password
DB_URL=jdbc:postgresql://db:5432/pku

# Security
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=86400

# Mail (for notifications)
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

---

## 🖼 Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   PostgreSQL    │
│   (Future)      │◄──►│   REST API      │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │   Flyway        │
                       │   Migrations    │
                       └─────────────────┘
```

---

## 🔧 Development Notes

### Database Schema
The application uses Flyway migrations (V1-V16) with comprehensive schema:
- Patient profiles and medical data
- Menu planning with nutritional calculations  
- Product catalog with allergen information
- Notification and sharing systems

### Performance Optimizations
- Database indexes on frequently queried columns
- Pagination for all list endpoints
- Connection pooling with HikariCP
- Calculated nutritional values with triggers

---

## 🚀 Future Enhancements

- [ ] **JWT Authentication** with refresh tokens
- [ ] **Frontend UI** (React/Vue.js)
- [ ] **Email notifications** for meal reminders
- [ ] **Mobile app** integration
- [ ] **Advanced analytics** and reporting
- [ ] **Multi-language support**
- [ ] **Recipe recommendations** based on PHE limits

---

## 👤 Author
Developed by [**ChubiniShato**](https://github.com/ChubiniShato)

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
