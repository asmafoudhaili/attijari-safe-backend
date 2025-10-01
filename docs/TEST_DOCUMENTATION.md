# Test Documentation

This document provides detailed documentation for all tests in the Attijari Safe Backend project.

## 📊 Test Overview

| Test Category | Status | Count | Coverage |
|---------------|--------|-------|----------|
| Unit Tests | ✅ PASSING | 22 | High |
| Integration Tests | 📝 AVAILABLE | 0 | - |
| E2E Tests | 📝 AVAILABLE | 0 | - |

## 🧪 Unit Tests

### UserServiceTest

**Location**: `src/test/java/com/example/backend/service/UserServiceTest.java`

**Purpose**: Tests the UserService business logic in isolation using mocked dependencies.

**Test Methods** (8 tests):

1. **`shouldReturnAllUsers()`**
   - **Purpose**: Verify that getAllUsers() returns all users from repository
   - **Mock Setup**: Mock UserRepository.findAll() to return test data
   - **Assertions**: Check returned list size and content

2. **`shouldReturnUserWhenGetUserByIdWithValidId()`**
   - **Purpose**: Verify getUserById() returns correct user for valid ID
   - **Mock Setup**: Mock UserRepository.findById() to return Optional.of(testUser)
   - **Assertions**: Check returned user matches expected user

3. **`shouldReturnEmptyWhenGetUserByIdWithInvalidId()`**
   - **Purpose**: Verify getUserById() returns empty Optional for invalid ID
   - **Mock Setup**: Mock UserRepository.findById() to return Optional.empty()
   - **Assertions**: Check returned Optional is empty

4. **`shouldCreateAndReturnUser()`**
   - **Purpose**: Verify createUser() saves and returns user
   - **Mock Setup**: Mock UserRepository.save() to return saved user
   - **Assertions**: Check returned user matches input user

5. **`shouldUpdateAndReturnUserWhenUpdateUserWithValidId()`**
   - **Purpose**: Verify updateUser() updates existing user
   - **Mock Setup**: Mock findById() and save() methods
   - **Assertions**: Check updated user fields match input

6. **`shouldReturnEmptyWhenUpdateUserWithInvalidId()`**
   - **Purpose**: Verify updateUser() returns empty for non-existent user
   - **Mock Setup**: Mock UserRepository.findById() to return empty
   - **Assertions**: Check returned Optional is empty

7. **`shouldDeleteUser()`**
   - **Purpose**: Verify deleteUser() calls repository delete method
   - **Mock Setup**: Mock UserRepository.deleteById()
   - **Assertions**: Verify deleteById() is called with correct ID

8. **`shouldHandleRepositoryExceptionDuringUserCreation()`**
   - **Purpose**: Verify exception handling during user creation
   - **Mock Setup**: Mock UserRepository.save() to throw RuntimeException
   - **Assertions**: Check exception is propagated correctly

### JwtUtilTest

**Location**: `src/test/java/com/example/backend/util/JwtUtilTest.java`

**Purpose**: Tests JWT utility functions for token generation, validation, and extraction.

**Test Methods** (14 tests):

#### Token Generation Tests

1. **`shouldGenerateValidTokenWithUsernameAndRoles()`**
   - **Purpose**: Verify token generation with valid inputs
   - **Test Data**: Username "testuser", roles ["ROLE_ADMIN", "ROLE_USER"]
   - **Assertions**: Check token is not null, username/roles extracted correctly

2. **`shouldThrowExceptionWhenGeneratingTokenWithNullUsername()`**
   - **Purpose**: Verify validation for null username
   - **Test Data**: null username, valid roles
   - **Assertions**: Check IllegalArgumentException is thrown

3. **`shouldThrowExceptionWhenGeneratingTokenWithEmptyUsername()`**
   - **Purpose**: Verify validation for empty username
   - **Test Data**: empty string username, valid roles
   - **Assertions**: Check IllegalArgumentException is thrown

4. **`shouldThrowExceptionWhenGeneratingTokenWithNullRoles()`**
   - **Purpose**: Verify validation for null roles
   - **Test Data**: valid username, null roles
   - **Assertions**: Check IllegalArgumentException is thrown

#### Token Extraction Tests

5. **`shouldExtractUsernameFromValidToken()`**
   - **Purpose**: Verify username extraction from valid token
   - **Test Data**: Generated token with known username
   - **Assertions**: Check extracted username matches expected

6. **`shouldThrowExceptionWhenExtractingUsernameFromInvalidToken()`**
   - **Purpose**: Verify error handling for invalid token
   - **Test Data**: Malformed token string
   - **Assertions**: Check IllegalArgumentException is thrown

7. **`shouldExtractRolesFromValidToken()`**
   - **Purpose**: Verify role extraction from valid token
   - **Test Data**: Generated token with known roles
   - **Assertions**: Check extracted roles match expected

8. **`shouldThrowExceptionWhenExtractingRolesFromInvalidToken()`**
   - **Purpose**: Verify error handling for invalid token
   - **Test Data**: Malformed token string
   - **Assertions**: Check IllegalArgumentException is thrown

9. **`shouldExtractIssuedAtFromValidToken()`**
   - **Purpose**: Verify issued-at date extraction
   - **Test Data**: Generated token
   - **Assertions**: Check extracted date is not null and reasonable

#### Token Validation Tests

10. **`shouldValidateTokenSuccessfully()`**
    - **Purpose**: Verify token validation for valid token
    - **Test Data**: Generated valid token
    - **Assertions**: Check validation returns true

11. **`shouldValidateTokenWithUsernameSuccessfully()`**
    - **Purpose**: Verify token validation with username check
    - **Test Data**: Generated token with known username
    - **Assertions**: Check validation returns true

12. **`shouldReturnFalseWhenValidatingTokenWithWrongUsername()`**
    - **Purpose**: Verify validation fails for wrong username
    - **Test Data**: Valid token, wrong username
    - **Assertions**: Check validation returns false

13. **`shouldReturnFalseWhenValidatingInvalidToken()`**
    - **Purpose**: Verify validation fails for invalid token
    - **Test Data**: Malformed token string
    - **Assertions**: Check validation returns false

14. **`shouldReturnFalseWhenValidatingInvalidTokenWithUsername()`**
    - **Purpose**: Verify validation fails for invalid token with username
    - **Test Data**: Malformed token string, valid username
    - **Assertions**: Check IllegalArgumentException is thrown

## 🔧 Test Utilities

### TestDataFactory

**Location**: `src/test/java/com/example/backend/TestDataFactory.java`

**Purpose**: Centralized factory for creating test data objects.

**Methods**:

- `createUser(String username, Role role)` - Creates basic user
- `createUser(String username, Role role, String mobileNumber, Gender gender, String avatar)` - Creates user with all fields
- `createAdminUser()` - Creates admin user
- `createClientUser()` - Creates client user

### BaseTestConfiguration

**Location**: `src/test/java/com/example/backend/BaseTestConfiguration.java`

**Purpose**: Base configuration for all test classes.

**Features**:
- Test-specific password encoder
- Common test beans

## 📈 Test Coverage

### Current Coverage

- **UserService**: 100% method coverage
- **JwtUtil**: 100% method coverage
- **Overall**: High coverage of critical business logic

### Coverage Reports

Generate coverage report:
```bash
mvn test jacoco:report
```

View report: `target/site/jacoco/index.html`

## 🚀 Running Tests

### All Unit Tests
```bash
mvn test -Dtest="UserServiceTest,JwtUtilTest"
```

### Specific Test Class
```bash
mvn test -Dtest="UserServiceTest"
```

### Specific Test Method
```bash
mvn test -Dtest="UserServiceTest#shouldCreateAndReturnUser"
```

### With Coverage
```bash
mvn test jacoco:report
```

## 🐛 Test Debugging

### Enable Debug Logging
Add to `application-test.properties`:
```properties
logging.level.com.example.backend=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Common Issues

1. **Test Data Issues**
   - Ensure test data is properly isolated
   - Use @BeforeEach for setup

2. **Mock Issues**
   - Verify mock setup matches actual method calls
   - Check mock interactions with verify()

3. **Assertion Issues**
   - Use AssertJ for better error messages
   - Check object equality vs reference equality

## 📝 Test Best Practices

### Naming Conventions
- Test methods: `should[ExpectedBehavior]When[StateUnderTest]()`
- Test classes: `[ClassName]Test`
- Test data: Use descriptive names in TestDataFactory

### Test Structure (AAA Pattern)
```java
@Test
void shouldReturnUserWhenGetUserByIdWithValidId() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    
    // Act
    Optional<User> result = userService.getUserById(1L);
    
    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testUser);
}
```

### Mocking Guidelines
- Mock external dependencies only
- Use @Mock for individual dependencies
- Use @InjectMocks for the class under test
- Verify important interactions

### Assertion Guidelines
- Use AssertJ for fluent assertions
- Test both positive and negative cases
- Verify all important return values
- Check exception types and messages

## 🔄 Continuous Integration

### Test Execution in CI
```yaml
- name: Run Tests
  run: mvn test
  
- name: Generate Coverage Report
  run: mvn jacoco:report
  
- name: Upload Coverage
  uses: codecov/codecov-action@v3
```

### Quality Gates
- All unit tests must pass
- Coverage threshold: 80%
- No critical issues in test code

---

**Last Updated**: September 2024
**Test Count**: 22 unit tests (all passing)
**Coverage**: High (business logic layer)
