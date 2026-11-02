# SimpleWallet

A secure and extensible wallet management REST API built with Spring Boot. SimpleWallet is designed to manage user accounts, authentication, wallet balances, deposits, transactions, and payment operations.

> **Project Status:** In Development 🚧

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Clone the Repository](#1-clone-the-repository)
    - [Configure the Database](#2-configure-the-database)
    - [Build the Application](#3-build-the-application)
    - [Run the Application](#4-run-the-application)
- [API Documentation](#api-documentation)
- [Authentication and Security](#authentication-and-security)
- [Development Guidelines](#development-guidelines)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
---
## Overview

SimpleWallet is a backend application focused on secure user authentication and digital wallet management. It follows a modular project structure to keep business logic organized and make future development easier.

The project uses Spring Security for authentication and authorization, Spring Data JPA for database operations, and Mysql for persistent data storage.

## Features

### Implemented / In Progress
- User entity and account information management
- User registration and login
- Password hashing
- Role-based authorization
- JWT-based authentication
- RESTful API architecture
- Automatic creation and update timestamps

### Planned Features
- User profile management
- Wallet creation and balance management
- Deposit requests and approval workflows
- Transaction history
- Payment method management
- Product and order management
- Admin dashboard APIs
- Input validation and centralized exception handling
- API documentation with OpenAPI/Swagger

*Feature availability depends on the current implementation.*

## Tech Stack

| Technology | Purpose |
|---|---|
| Java | Programming language |
| Spring Boot | Backend application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| Mysql | Relational database |
| Maven | Dependency and build management |
| Lombok | Boilerplate code reduction |
| JWT | Token-based authentication |
| Bean Validation | Request validation |

## Project Structure

```text
simpleWallet/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── io/github/parthadevs/simpleWallet/
    │   │       ├── SimpleWalletApplication.java
    │   │       ├── config/
    │   │       ├── security/
    │   │       ├── common/
    │   │       ├── auth/
    │   │       ├── user/
    │   │       ├── wallet/
    │   │       ├── transaction/
    │   │       ├── deposit/
    │   │       ├── payment/
    │   │       ├── product/
    │   │       ├── category/
    │   │       └── order/
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
```
[Full File Structure](./file-structure.md)

Some modules represent planned functionality and may not exist in the current codebase yet.

## Getting Started

### Prerequisites

Make sure you have installed:

- Java 21 or the version configured in `pom.xml`
- Maven 3.9+
- Mysql
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/parthadevs/simpleWallet.git
cd simpleWallet
```

### 2. Configure the Database

Create a Mysql database:

```sql
CREATE DATABASE simplewallet;
```

Configure your database connection in `src/main/resources/application.properties`.

Example:

```properties
spring.application.name=simpleWallet

application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=86400000

spring.datasource.url=jdbc:mysql://localhost:3306/simplewallet
spring.datasource.username=root
spring.datasource.password=Database_password

  # JPA / Hibernate Configurations
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Security:** Never commit real database credentials, JWT secrets, or other sensitive environment variables to GitHub.

For production deployments, use a proper database migration tool such as Flyway or Liquibase instead of relying on `ddl-auto: update`.

### 3. Build the Application

```bash
./mvnw clean package
```

If the project does not include the Maven Wrapper, use:

```bash
mvn clean package
```

### 4. Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

Or run the packaged JAR:

```bash
java -jar target/*.jar
```

The server will use the configured port. If no custom port is configured, Spring Boot typically starts on:

```text
http://localhost:8080
```

## API Documentation

The API follows REST conventions and uses JSON for request and response bodies.

Planned endpoint groups:

| Module | Example Endpoint | Purpose |
|---|---|---|
| Authentication | `/api/v1/auth/register` | Register an account |
| Authentication | `/api/v1/auth/login` | Authenticate a user |
| User | `/api/v1/users/me` | Retrieve the authenticated user's profile |
| Wallet | `/api/v1/wallet` | Retrieve wallet information |
| Transactions | `/api/v1/transactions` | Retrieve transaction history |
| Deposits | `/api/v1/deposits` | Manage deposit requests |
| Products | `/api/v1/products` | Browse products |
| Orders | `/api/v1/orders` | Manage orders |

[SimpleWallet API Full Documentation](./simplewallet-api-docs.md)

These paths are proposed examples, not a guarantee of currently implemented routes. Update this table to match the actual controllers and mappings.

## Authentication and Security

SimpleWallet is designed to use Spring Security and JWT-based authentication.

Security considerations include:

- Hash passwords using a secure password encoder such as BCrypt.
- Validate and authorize protected API requests.
- Restrict administrative endpoints by role.
- Validate incoming request data.
- Never expose password hashes or secret credentials in API responses.
- Protect wallet operations against duplicate requests and unauthorized balance changes.
- Use database transactions to maintain consistency between wallet balances and transaction records.

## Development Guidelines

The project follows a modular structure:

```text
Controller → Service → Repository → Database
```

- **Controller:** Handles HTTP requests and responses.
- **Service:** Contains business logic.
- **Repository:** Performs database operations.
- **Entity:** Represents persisted domain data.
- **DTO:** Defines API request and response structures.

Financial operations should be implemented with appropriate transaction boundaries, concurrency control, and an auditable transaction history.

## Testing

Run the automated tests with:

```bash
./mvnw test
```

Recommended test coverage includes:

- Registration and login
- Invalid credentials
- Role-based access control
- Wallet balance updates
- Deposit approval and rejection
- Duplicate transaction prevention
- Concurrent wallet operations

## Roadmap

- [x] Complete authentication and JWT security
- [x] Implement user profile APIs
- [x] Implement wallet creation and balance operations
- [ ] Implement deposits and withdrawals
- [x] Add transaction history
- [ ] Implement payment methods
- [ ] Add product and order management
- [ ] Add administrative APIs
- [ ] Add API documentation
- [ ] Add integration tests
- [ ] Add Docker support
- [ ] Prepare production deployment configuration

## Contributing

Contributions, suggestions, and bug reports are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Submit a pull request with a clear description.

## License
This project is licensed under the MIT License.

---

**Maintained by [Partha Sarker](https://github.com/parthadevs)**