# Contributing to PKU Diet App

Thank you for your interest in contributing to the PKU Diet App! We welcome contributions from the community to help improve dietary management for PKU patients.

This document provides guidelines and instructions for contributing to this repository.

## Table of Contents
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
  - [Branching Strategy](#branching-strategy)
  - [Commit Conventions](#commit-conventions)
  - [Code Style](#code-style)
- [Pull Request Process](#pull-request-process)
- [Testing](#testing)

## Getting Started

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** locally:
    ```bash
    git clone https://github.com/YOUR_USERNAME/pku-diet-app.git
    cd pku-diet-app
    ```
3.  **Set up the environment**:
    - Follow the [Local Development](README.md#local-development) guide in the main README.
    - Ensure you have Java 21, Maven 3.6+, and Docker installed.
    - Copy `.env.example` to `.env` and configure your local environment variables.

## Development Workflow

### Branching Strategy

We use a simple branching model based on the type of change:

-   `feature/description`: For new features (e.g., `feature/add-recipe-support`).
-   `fix/description`: For bug fixes (e.g., `fix/login-error`).
-   `chore/description`: For maintenance, refactoring, or documentation (e.g., `chore/update-dependencies`).
-   `docs/description`: For documentation-only changes.

**Always create your branch from `main`.**

```bash
git checkout main
git pull origin main
git checkout -b feature/cool-new-feature
```

### Commit Conventions

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. This helps us generate changelogs and version releases automatically.

Format: `<type>(<scope>): <description>`

Examples:
-   `feat(menu): add weekly menu generation algorithm`
-   `fix(api): handle null pointer in ProductService`
-   `docs(readme): update deployment instructions`
-   `style(checkstyle): fix formatting violations`
-   `refactor(product): introduce ProductMapper for DTO mapping`
-   `test(e2e): add test for localized product search`

### Code Style

We use **Spotless** to enforce code formatting for Java.

Before committing, run:

```bash
cd services/api
mvn spotless:apply
```

This will automatically format your code to match the project's style guidelines. CI will fail if there are formatting violations.

## Pull Request Process

1.  **Push your branch** to your fork:
    ```bash
    git push origin feature/cool-new-feature
    ```
2.  **Open a Pull Request** (PR) against the `main` branch of the original repository.
3.  **Fill out the PR Template**:
    -   **Description**: What does this PR do?
    -   **Related Issue**: Link to the issue this PR addresses (e.g., `Fixes #123`).
    -   **Type of Change**: Feature, Bug fix, Documentation, etc.
    -   **Verification**: How did you test this change? (Add screenshots for UI changes).
4.  **Wait for Review**: The maintainers will review your code. Be ready to address feedback.
5.  **Merge**: Once approved and all checks pass, your PR will be merged!

## Testing

Please ensure your changes are covered by tests.

-   **Unit Tests**: Run `mvn test` in `services/api`.
-   **Integration Tests**: Run `mvn verify -Pintegration`.
-   **E2E Tests**: See [E2E Testing Guide](E2E_TESTING_README.md) for running Playwright tests.
-   **Performance Tests**: See [Performance Testing Guide](perf/README.md) for running k6 tests.

Thank you for contributing! 🚀
