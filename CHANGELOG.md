# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Product Mapper**: Introduced MapStruct `ProductMapper` interface to handle Entity-DTO conversions consistently.
- **Documentation**: Added comprehensive `CONTRIBUTING.md` guidelines.
- **Documentation**: Added Documentation Index and Mermaid architecture diagram to `README.md`.
- **Testing**: Added `ProductMapperTest` to verify mapping logic.

### Changed
- **Refactoring**: Updated `ProductService` to use `ProductMapper`, removing ~40 lines of boilerplate code.
- **Cleanup**: Removed redundant `ProductNotFoundException` handling in `ProductController` (now handled globally).
- **Tests**: Updated `MultiLanguageSupportTest` and integration tests to support the new mapper architecture.

### Fixed
- **CI/CD**: Fixed Vite dev server startup in E2E workflow (PR #14).

## [0.1.0] - 2025-10-15

### Added
- Initial release of PKU Diet App API.
- Patient Management module (CRUD, Norms).
- Menu Planning module (Weekly/Daily generation).
- Product Catalog with Nutritional Data.
- Multi-language support (ka, ru, en).
- Basic security configuration (Spring Security).
