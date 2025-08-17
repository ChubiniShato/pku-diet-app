# PKU Diet App

A Spring Boot application for managing PKU (Phenylketonuria) diet planning with support for Georgian text and UTF-8 encoding.

## UTF-8 Encoding Setup

This application is configured to properly handle Georgian text and other Unicode characters. The following configurations ensure proper UTF-8 encoding:

### Database Configuration
- PostgreSQL connection uses UTF-8 encoding
- Hibernate configured for UTF-8 character encoding
- Database tables support Unicode text

### Application Configuration
- Spring Boot configured with UTF-8 message converters
- Docker container set with UTF-8 environment variables
- File encoding set to UTF-8

### For Developers
When adding Georgian text or other Unicode content:
1. Ensure all source files are saved with UTF-8 encoding
2. Use proper Unicode escape sequences if needed
3. Test text display in both development and production environments

## Running the Application

```bash
# Using Docker Compose
docker-compose up

# Using Maven
mvn spring-boot:run
```

## Database Migrations

The application uses Flyway for database migrations. All SQL files are saved with UTF-8 encoding to support Georgian text.
