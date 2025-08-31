# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Integration tests with Testcontainers for PostgreSQL
- OpenAPI JSON export via Maven plugin
- Docker setup with distroless images and healthchecks
- GitHub Actions CI/CD pipelines
- Environment configuration template
- OWASP Dependency Check integration
- Performance test harness with k6
- Comprehensive documentation

### Changed
- Updated Dockerfile to use distroless base image
- Enhanced docker-compose.yml with better healthchecks
- Improved Maven build configuration

### Fixed
- Various bug fixes and improvements

## [0.0.1] - 2024-01-XX

### Added
- Initial PKU diet planning API
- Menu generation with heuristic algorithms
- Patient profile and norms management
- Product database with nutritional information
- Label scanning with OCR integration
- Sharing and consent management
- Notification system (email, SMS, push)
- Validation engine for nutritional compliance
- Multi-language support (English, Georgian, Russian)
- RESTful API with OpenAPI documentation

### Security
- JWT-based authentication
- Role-based access control
- Data encryption and masking
- Rate limiting and security headers

---

## Commit Convention

This project follows [Conventional Commits](https://conventionalcommits.org/) specification:

### Types
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `build`: Changes that affect the build system or external dependencies
- `ci`: Changes to our CI configuration files and scripts
- `chore`: Other changes that don't modify src or test files

### Examples
```
feat: add menu generation algorithm
fix: resolve validation bug in daily menu creation
docs: update API documentation
test: add integration tests for menu validation
```
