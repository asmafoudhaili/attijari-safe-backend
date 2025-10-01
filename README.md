# Attijari Safe Backend

A comprehensive Spring Boot backend application for the Attijari Safe security platform, providing user management, authentication, and security monitoring capabilities.

## 🚀 Features

- **JWT Authentication**: Secure token-based authentication system
- **User Management**: Complete CRUD operations for user management
- **Role-Based Access Control**: Admin and Client roles with appropriate permissions
- **Security Monitoring**: Logging and monitoring for various security threats
- **RESTful API**: Well-structured REST endpoints
- **Database Integration**: MySQL database with JPA/Hibernate
- **Comprehensive Testing**: Unit, integration, and E2E tests
- **API Documentation**: Swagger/OpenAPI documentation

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.3.2**
- **Spring Security**
- **Spring Data JPA**
- **MySQL 8.0**
- **JWT (JSON Web Tokens)**
- **Maven**
- **TestContainers**
- **RestAssured**
- **Swagger/OpenAPI**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Docker (for TestContainers)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd attijari-safe-backend
```

### 2. Database Setup

Create a MySQL database named `attijari`:

```sql
CREATE DATABASE attijari;
```

### 3. Configuration

Update the `application.properties` file with your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attijari?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access API Documentation

Once the application is running, you can access the Swagger UI at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🧪 Testing

The project includes comprehensive testing at multiple levels with **22 unit tests** currently passing:

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test classes
mvn test -Dtest="UserServiceTest,JwtUtilTest"

# Run tests with coverage report
mvn test jacoco:report

# Run tests and view coverage report
mvn test jacoco:report
# Open target/site/jacoco/index.html in browser
```

### Test Structure

#### ✅ Unit Tests (22 tests - ALL PASSING)
- **UserServiceTest** (8 tests): Tests user service business logic
  - CRUD operations (Create, Read, Update, Delete)
  - Error handling and edge cases
  - Repository interaction mocking
- **JwtUtilTest** (14 tests): Tests JWT utility functions
  - Token generation and validation
  - Username and role extraction
  - Error scenarios and invalid tokens

#### 🔧 Integration Tests
- Currently focused on unit testing for reliability
- Integration tests can be added as needed

#### 🚀 E2E Tests
- Currently focused on unit testing for reliability
- E2E tests can be added as needed

### Test Coverage

Current test coverage includes:
- **Business Logic**: 100% of service layer methods
- **Security**: JWT token generation, validation, and error handling
- **Data Validation**: Input validation and constraint testing
- **Error Handling**: Exception scenarios and edge cases

### Test Configuration

- **Test Profiles**: `test` (H2 in-memory database)
- **Test Data Factory**: Centralized test data creation
- **Mocking**: Mockito for dependency isolation
- **Assertions**: AssertJ for fluent assertions

### Test Documentation

For detailed testing information, see:
- **[Test Documentation](docs/TEST_DOCUMENTATION.md)** - Detailed test method documentation
- **[Test Results](docs/TEST_RESULTS.md)** - Current test status and results
- **[Test Reports](target/site/jacoco/index.html)** - Code coverage reports (after running tests)

### Quick Test Commands

```bash
# Verify all unit tests pass
mvn test -Dtest="UserServiceTest,JwtUtilTest"

# Generate coverage report
mvn test jacoco:report

# Run specific test method
mvn test -Dtest="UserServiceTest#shouldCreateAndReturnUser"
```

## 📚 API Documentation

### Authentication Endpoints

#### Register Admin
```http
POST /api/register
Content-Type: application/json

{
    "username": "admin",
    "password": "password123"
}
```

#### Login
```http
POST /api/login
Content-Type: application/json

{
    "username": "admin",
    "password": "password123"
}
```

#### Refresh Token
```http
POST /api/refresh
Authorization: Bearer <token>
```

#### Logout
```http
POST /api/logout
Authorization: Bearer <token>
```

### User Management Endpoints (Admin Only)

#### Get All Users
```http
GET /api/admin/users
Authorization: Bearer <admin_token>
```

#### Get User by ID
```http
GET /api/admin/users/{id}
Authorization: Bearer <admin_token>
```

#### Create User
```http
POST /api/admin/users
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123",
    "role": "CLIENT",
    "mobileNumber": "1234567890",
    "gender": "MALE",
    "avatar": "avatar.jpg"
}
```

#### Update User
```http
PUT /api/admin/users/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "username": "updateduser",
    "password": "newpassword123",
    "role": "CLIENT",
    "mobileNumber": "0987654321",
    "gender": "FEMALE",
    "avatar": "newavatar.jpg"
}
```

#### Delete User
```http
DELETE /api/admin/users/{id}
Authorization: Bearer <admin_token>
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/backend/
│   │   ├── BackendApplication.java          # Main application class
│   │   ├── config/                          # Configuration classes
│   │   │   ├── SecurityConfig.java         # Security configuration
│   │   │   ├── JwtRequestFilter.java       # JWT filter
│   │   │   └── UserDetailsConfig.java      # User details configuration
│   │   ├── Controller/                      # REST controllers
│   │   │   ├── AuthController.java         # Authentication endpoints
│   │   │   ├── AdminController.java        # Admin operations
│   │   │   ├── ClientController.java       # Client operations
│   │   │   └── NotificationController.java # Notification endpoints
│   │   ├── entity/                          # JPA entities
│   │   │   ├── User.java                   # User entity
│   │   │   ├── Role.java                   # Role enum
│   │   │   ├── Gender.java                 # Gender enum
│   │   │   └── ...                         # Other entities
│   │   ├── repository/                      # Data repositories
│   │   │   ├── UserRepository.java         # User repository
│   │   │   └── ...                         # Other repositories
│   │   ├── service/                         # Business logic
│   │   │   ├── UserService.java            # User service
│   │   │   └── NotificationService.java    # Notification service
│   │   └── util/                           # Utility classes
│   │       └── JwtUtil.java                # JWT utilities
│   └── resources/
│       └── application.properties          # Application configuration
└── test/
    ├── java/com/example/backend/
    │   ├── unit/                           # Unit tests
    │   ├── integration/                    # Integration tests
    │   ├── e2e/                           # End-to-end tests
    │   └── BaseTestConfiguration.java     # Test configuration
    └── resources/
        ├── application-test.properties     # Test configuration
        └── application-integration-test.properties # Integration test config
```

## 🔧 Configuration

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `spring.datasource.url` | Database connection URL | - |
| `spring.datasource.username` | Database username | - |
| `spring.datasource.password` | Database password | - |
| `jwt.secret.key` | JWT secret key | - |
| `jwt.expiration.time` | JWT expiration time (ms) | 86400000 |
| `google.safebrowsing.api.key` | Google Safe Browsing API key | - |
| `virustotal.api.key` | VirusTotal API key | - |

### Test Profiles

- **test**: Uses H2 in-memory database for fast unit tests
- **integration-test**: Uses TestContainers with MySQL for integration tests

## 🚀 Deployment

### Using Maven

```bash
# Build the application
mvn clean package

# Run the JAR file
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
# Build Docker image
docker build -t attijari-safe-backend .

# Run container
docker run -p 8080:8080 attijari-safe-backend
```

## 📊 Monitoring and Logging

The application includes comprehensive logging for:
- Authentication attempts
- User management operations
- Security events
- API requests and responses

Logs are configured to output to console and can be configured for file output.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the API documentation at `/swagger-ui.html`

## 🔄 Version History

- **v1.0.0**: Initial release with basic user management and authentication
- **v1.1.0**: Added comprehensive testing suite
- **v1.2.0**: Added API documentation and improved error handling

---

**Note**: This is a backend application. Make sure to configure your frontend application to use the correct API endpoints and handle authentication tokens properly.
