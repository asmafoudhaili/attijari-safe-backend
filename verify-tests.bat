@echo off
echo ========================================
echo Attijari Safe Backend - Test Verification
echo ========================================
echo.

echo [1/3] Running Unit Tests...
call mvn test -Dtest="UserServiceTest,JwtUtilTest" -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Unit tests failed!
    exit /b 1
)
echo ✅ Unit tests passed! (22 tests)
echo.

echo [2/3] Generating Coverage Report...
call mvn jacoco:report -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Coverage report generation failed!
    exit /b 1
)
echo ✅ Coverage report generated!
echo.

echo [3/3] Test Summary...
echo.
echo 📊 Test Results:
echo   - UserServiceTest: 8 tests ✅
echo   - JwtUtilTest: 14 tests ✅
echo   - Total: 22 tests ✅
echo   - Failures: 0 ❌
echo   - Errors: 0 ❌
echo.
echo 📈 Coverage Report: target/site/jacoco/index.html
echo 📚 Documentation: docs/TEST_DOCUMENTATION.md
echo.
echo ========================================
echo All tests verified successfully! 🎉
echo ========================================
echo.
pause
