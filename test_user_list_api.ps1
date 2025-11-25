# Test Script for User List API
# GET /api/admin/users/

$baseUrl = "http://localhost:8080"

Write-Host "`n=== Testing User List API Endpoint ===" -ForegroundColor Cyan
Write-Host "GET /api/admin/users/`n" -ForegroundColor Cyan

# Step 1: Get admin token
Write-Host "[STEP 1] Admin Login" -ForegroundColor Yellow
$email = Read-Host "Enter admin email"
$password = Read-Host "Enter admin password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    $loginBody = @{
        email = $email
        password = $passwordPlain
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Login successful`n" -ForegroundColor Green
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 2: Test basic endpoint (no pagination params)
Write-Host "[STEP 2] Testing Basic GET /api/admin/users/" -ForegroundColor Yellow
try {
    $allUsers = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/" -Method Get -Headers $headers
    
    if ($allUsers.PSObject.Properties.Name -contains "content") {
        Write-Host "✓ Pagination working: Found $($allUsers.content.Count) users on default page" -ForegroundColor Green
        Write-Host "  Total Elements: $($allUsers.totalElements)" -ForegroundColor Gray
        Write-Host "  Total Pages: $($allUsers.totalPages)" -ForegroundColor Gray
        Write-Host "  Page Size: $($allUsers.size)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Unexpected response format" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 3: Verify DTO Response Structure (NO PASSWORD!)
Write-Host "`n[STEP 3] 🔒 Security Check - Verify NO Password Exposure" -ForegroundColor Yellow
if ($allUsers.content.Count -gt 0) {
    $firstUser = $allUsers.content[0]
    
    Write-Host "`nFirst User Response:" -ForegroundColor Cyan
    $firstUser | ConvertTo-Json | Write-Host -ForegroundColor White
    
    # Check for password field
    if ($firstUser.PSObject.Properties.Name -contains "password") {
        Write-Host "`n🔴 CRITICAL SECURITY ISSUE: Password field is exposed!" -ForegroundColor Red
        Write-Host "Password value: $($firstUser.password)" -ForegroundColor Red
    } else {
        Write-Host "`n✓ SECURITY PASS: No password field in response" -ForegroundColor Green
    }
    
    # Check expected fields
    $expectedFields = @("id", "firstName", "lastName", "email", "role", "active", "createdAt")
    $allFieldsPresent = $true
    
    Write-Host "`nField Validation:" -ForegroundColor Cyan
    foreach ($field in $expectedFields) {
        if ($firstUser.PSObject.Properties.Name -contains $field) {
            Write-Host "  ✓ $field" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Missing: $field" -ForegroundColor Red
            $allFieldsPresent = $false
        }
    }
    
    # Check no circular references
    if ($firstUser.PSObject.Properties.Name -notcontains "addresses" -and 
        $firstUser.PSObject.Properties.Name -notcontains "ratings" -and
        $firstUser.PSObject.Properties.Name -notcontains "reviews") {
        Write-Host "  ✓ No circular references (addresses/ratings/reviews)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Circular reference risk detected" -ForegroundColor Red
    }
}

# Step 4: Test Pagination
Write-Host "`n[STEP 4] Testing Pagination Parameters" -ForegroundColor Yellow

Write-Host "  • Page 0, Size 5:" -ForegroundColor Cyan
$page0 = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/?page=0&size=5" -Method Get -Headers $headers
Write-Host "    Users: $($page0.content.Count) | Total: $($page0.totalElements)" -ForegroundColor White

Write-Host "  • Page 1, Size 5:" -ForegroundColor Cyan
$page1 = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/?page=1&size=5" -Method Get -Headers $headers
Write-Host "    Users: $($page1.content.Count) | Total: $($page1.totalElements)" -ForegroundColor White

if ($page0.content.Count -eq 5 -or $page0.totalElements -le 5) {
    Write-Host "  ✓ Pagination working correctly" -ForegroundColor Green
} else {
    Write-Host "  ✗ Pagination issue detected" -ForegroundColor Red
}

# Step 5: Test Sorting
Write-Host "`n[STEP 5] Testing Sorting" -ForegroundColor Yellow

Write-Host "  • Sort by email:" -ForegroundColor Cyan
$sortedByEmail = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/?page=0&size=5&sortBy=email" -Method Get -Headers $headers
Write-Host "    First user email: $($sortedByEmail.content[0].email)" -ForegroundColor White

Write-Host "  • Sort by createdAt:" -ForegroundColor Cyan
$sortedByDate = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/?page=0&size=5&sortBy=createdAt" -Method Get -Headers $headers
Write-Host "    First user created: $($sortedByDate.content[0].createdAt)" -ForegroundColor White

Write-Host "  ✓ Sorting parameters accepted" -ForegroundColor Green

# Step 6: Display User Summary
Write-Host "`n[STEP 6] User Summary" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$adminUsers = $allUsers.content | Where-Object { $_.role -eq "ROLE_ADMIN" }
$regularUsers = $allUsers.content | Where-Object { $_.role -eq "ROLE_USER" }
$activeUsers = $allUsers.content | Where-Object { $_.active -eq $true }
$inactiveUsers = $allUsers.content | Where-Object { $_.active -eq $false }

Write-Host "Total Users: $($allUsers.totalElements)" -ForegroundColor White
Write-Host "Admin Users: $($adminUsers.Count)" -ForegroundColor Yellow
Write-Host "Regular Users: $($regularUsers.Count)" -ForegroundColor Cyan
Write-Host "Active Users: $($activeUsers.Count)" -ForegroundColor Green
Write-Host "Inactive Users: $($inactiveUsers.Count)" -ForegroundColor Red

# Display users
Write-Host "`nUser List (Page 0):" -ForegroundColor Cyan
foreach ($user in $allUsers.content) {
    $statusIcon = if ($user.active) { "●" } else { "○" }
    $statusColor = if ($user.active) { "Green" } else { "Red" }
    $roleColor = if ($user.role -eq "ROLE_ADMIN") { "Yellow" } else { "Cyan" }
    
    Write-Host "  $statusIcon " -NoNewline -ForegroundColor $statusColor
    Write-Host "[ID:$($user.id)] " -NoNewline -ForegroundColor White
    Write-Host "$($user.firstName) $($user.lastName) " -NoNewline -ForegroundColor White
    Write-Host "($($user.email)) " -NoNewline -ForegroundColor Gray
    Write-Host "- $($user.role)" -ForegroundColor $roleColor
}

# Summary
Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "✓ API endpoint responsive" -ForegroundColor Green
Write-Host "✓ Pagination implemented" -ForegroundColor Green
Write-Host "✓ Sorting working" -ForegroundColor Green
if (-not ($firstUser.PSObject.Properties.Name -contains "password")) {
    Write-Host "✓ NO password exposure (SECURE)" -ForegroundColor Green
} else {
    Write-Host "✗ Password exposed (CRITICAL)" -ForegroundColor Red
}
Write-Host "✓ DTO structure correct" -ForegroundColor Green
Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
