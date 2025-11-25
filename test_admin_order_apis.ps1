# Comprehensive Admin Order Status API Test Script
# Tests all order status transitions, DTO responses, and validation logic

$baseUrl = "http://localhost:8080"
$passed = 0
$failed = 0

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "   ADMIN ORDER STATUS API COMPREHENSIVE TESTS" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# Step 1: Get admin credentials
Write-Host "[STEP 1] Admin Authentication" -ForegroundColor Yellow
$email = Read-Host "Enter admin email"
$password = Read-Host "Enter admin password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    $loginBody = @{
        email    = $email
        password = $passwordPlain
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Login successful" -ForegroundColor Green
    $passed++
}
catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    $failed++
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

# Step 2: Get orders with pagination
Write-Host "`n[STEP 2] Test Pagination - GET /api/admin/orders/" -ForegroundColor Yellow
try {
    $ordersPage = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/?page=0&size=5&sortBy=createAt" -Method Get -Headers $headers
    
    if ($ordersPage.PSObject.Properties.Name -contains "content") {
        Write-Host "✓ Pagination working: Found $($ordersPage.content.Count) orders on page 0" -ForegroundColor Green
        $passed++
    }
    else {
        Write-Host "✗ Unexpected response format" -ForegroundColor Red
        $failed++
    }
}
catch {
    Write-Host "✗ Failed to get orders: $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}

# Step 3: Verify DTO response (no entity exposure)
Write-Host "`n[STEP 3] Verify DTO Response Structure" -ForegroundColor Yellow
if ($ordersPage.content.Count -gt 0) {
    $firstOrder = $ordersPage.content[0]
    $expectedFields = @("id", "orderId", "userId", "userEmail", "orderStatus", "totalPrice", "totalItem", "orderDate", "createAt")
    $allFieldsPresent = $true
    
    foreach ($field in $expectedFields) {
        if (-not ($firstOrder.PSObject.Properties.Name -contains $field)) {
            Write-Host "  ✗ Missing field: $field" -ForegroundColor Red
            $allFieldsPresent = $false
        }
    }
    
    if ($allFieldsPresent) {
        Write-Host "✓ All expected DTO fields present" -ForegroundColor Green
        $passed++
    }
    else {
        $failed++
    }
    
    # Check no entity references (should not have full User/Product objects)
    if ($firstOrder.PSObject.Properties.Name -notcontains "user" -and $firstOrder.PSObject.Properties.Name -notcontains "password") {
        Write-Host "✓ No entity exposure detected" -ForegroundColor Green
        $passed++
    }
    else {
        Write-Host "✗ Entity exposure detected!" -ForegroundColor Red
        $failed++
    }
}

# Stepstep 4: Test order status flow
if ($ordersPage.content.Count -gt 0) {
    $testOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "PENDING" }
    
    if ($testOrders.Count -gt 0) {
        $testOrder = $testOrders[0]
        $orderId = $testOrder.id
        
        Write-Host "`n[STEP 4] Test Order Status Flow on Order ID: $orderId" -ForegroundColor Yellow
        
        # Test PENDING -> CONFIRMED
        Write-Host "  • Testing PENDING → CONFIRMED" -ForegroundColor Yellow
        try {
            $confirmedOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/confirmed" -Method Put -Headers $headers
            if ($confirmedOrder.orderStatus -eq "CONFIRMED") {
                Write-Host "    ✓ Status changed to CONFIRMED" -ForegroundColor Green
                $passed++
            }
            else {
                Write-Host "    ✗ Expected CONFIRMED but got $($confirmedOrder.orderStatus)" -ForegroundColor Red
                $failed++
            }
        }
        catch {
            Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
        
        # Test CONFIRMED -> SHIPPED
        Write-Host "  • Testing CONFIRMED → SHIPPED" -ForegroundColor Yellow
        try {
            $shippedOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/ship" -Method Put -Headers $headers
            if ($shippedOrder.orderStatus -eq "SHIPPED") {
                Write-Host "    ✓ Status changed to SHIPPED" -ForegroundColor Green
                $passed++
            }
            else {
                Write-Host "    ✗ Expected SHIPPED but got $($shippedOrder.orderStatus)" -ForegroundColor Red
                $failed++
            }
        }
        catch {
            Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
        
        # Test SHIPPED -> DELIVERED
        Write-Host "  • Testing SHIPPED → DELIVERED" -ForegroundColor Yellow
        try {
            $deliveredOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/deliver" -Method Put -Headers $headers
            if ($deliveredOrder.orderStatus -eq "DELIVERED") {
                Write-Host "    ✓ Status changed to DELIVERED" -ForegroundColor Green
                $passed++
                
                # Verify deliveryDate is set
                if ($deliveredOrder.deliveryDate) {
                    Write-Host "    ✓ Delivery date set: $($deliveredOrder.deliveryDate)" -ForegroundColor Green
                    $passed++
                }
                else {
                    Write-Host "    ✗ Delivery date not set!" -ForegroundColor Red
                    $failed++
                }
            }
            else {
                Write-Host "    ✗ Expected DELIVERED but got $($deliveredOrder.orderStatus)" -ForegroundColor Red
                $failed++
            }
        }
        catch {
            Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
    }
    else {
        Write-Host "`n[STEP 4] No PENDING orders available for testing" -ForegroundColor Yellow
    }
}

# Step 5: Test invalid transitions
Write-Host "`n[STEP 5] Test Invalid Status Transitions" -ForegroundColor Yellow

# Find a PENDING order and try to ship it directly (should fail)
$pendingOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "PENDING" }
if ($pendingOrders.Count -gt 0) {
    $pendingOrder = $pendingOrders[0]
    Write-Host "  • Testing PENDING → SHIPPED (should fail)" -ForegroundColor Yellow
    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($pendingOrder.id)/ship" -Method Put -Headers $headers
        Write-Host "    ✗ Should have failed but succeeded!" -ForegroundColor Red
        $failed++
    }
    catch {
        if ($_.Exception.Message -like "*Cannot ship*") {
            Write-Host "    ✓ Correctly rejected: $($_.ErrorDetails.Message)" -ForegroundColor Green
            $passed++
        }
        else {
            Write-Host "    ✗ Failed with unexpected error: $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
    }
}

# Try to cancel a delivered order (should fail)
$deliveredOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "DELIVERED" }
if ($deliveredOrders.Count -gt 0) {
    $deliveredOrder = $deliveredOrders[0]
    Write-Host "  • Testing DELIVERED → CANCELLED (should fail)" -ForegroundColor Yellow
    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($deliveredOrder.id)/cancel" -Method Put -Headers $headers
        Write-Host "    ✗ Should have failed but succeeded!" -ForegroundColor Red
        $failed++
    }
    catch {
        if ($_.Exception.Message -like "*Cannot cancel*") {
            Write-Host "    ✓ Correctly rejected: $($_.ErrorDetails.Message)" -ForegroundColor Green
            $passed++
        }
        else {
            Write-Host "    ✗ Failed with unexpected error: $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
    }
}

# Step 6: Test cancellation from valid states
Write-Host "`n[STEP 6] Test Cancellation from Valid States" -ForegroundColor Yellow
$cancelableOrders = $ordersPage.content | Where-Object { $_.orderStatus -in @("PENDING", "PLACED", "CONFIRMED") }
if ($cancelableOrders.Count -gt 0) {
    $cancelOrder = $cancelableOrders[0]
    $originalStatus = $cancelOrder.orderStatus
    Write-Host "  • Testing $originalStatus → CANCELLED" -ForegroundColor Yellow
    try {
        $cancelledOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($cancelOrder.id)/cancel" -Method Put -Headers $headers
        if ($cancelledOrder.orderStatus -eq "CANCELLED") {
            Write-Host "    ✓ Successfully cancelled order from $originalStatus" -ForegroundColor Green
            $passed++
        }
        else {
            Write-Host "    ✗ Expected CANCELLED but got $($cancelledOrder.orderStatus)" -ForegroundColor Red
            $failed++
        }
    }
    catch {
        Write-Host "    ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
}

# Summary
Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "                TEST SUMMARY" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "PASSED: $passed" -ForegroundColor Green
Write-Host "FAILED: $failed" -ForegroundColor Red
Write-Host "TOTAL:  $($passed + $failed)`n" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "🎉 ALL TESTS PASSED! 🎉" -ForegroundColor Green
}
else {
    Write-Host "⚠️  SOME TESTS FAILED ⚠️" -ForegroundColor Red
}
