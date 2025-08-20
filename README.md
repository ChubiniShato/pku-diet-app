# PKU Diet App

A web application to support dietary management for **Phenylketonuria (PKU)** patients.  
The backend provides product management APIs, CSV data import, UTF-8 encoding support, and database migrations.

---

## ✨ Features
- Upload food products via CSV
- Manage products through REST API
- UTF-8 encoding support (multilingual data)
- Centralized exception handling
- Flyway database migrations (schema & data)
- Dockerized API service

---

## 🛠 Tech Stack
- **Java 21**
- **Spring Boot**
- **Maven**
- **Flyway** (database migrations)
- **Docker**
- **PostgreSQL / H2 (for dev/test)**

---

## 🚀 Getting Started

### Prerequisites
- Java 21  
- Maven  
- Docker (optional, for containerized run)  

### Run locally
```bash
# clone the repository
git clone https://github.com/ChubiniShato/pku-diet-app.git
cd pku-diet-app/services/api

# build
mvn clean install

# run
mvn spring-boot:run
```

### Run with Docker
```bash
docker build -t pku-diet-app .
docker run -p 8080:8080 pku-diet-app
```

---

## 📡 API Endpoints (examples)

- `GET /products` → List all products  
- `GET /products/{id}` → Get product by ID  
- `POST /products/upload` → Upload CSV with products  

---

## 🖼 Future Improvements
- Authentication & Authorization (JWT / OAuth2)
- Frontend UI (React or Angular)
- Reporting & analytics module
- Docker Compose setup for full stack

---

## 👤 Author
Developed by [**ChubiniShato**](https://github.com/ChubiniShato)
