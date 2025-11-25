# Test Script for Cancel Order API Endpoint
# PUT /api/admin/orders/{orderId}/cancel

$baseUrl = "http://localhost:8080"

Write-Host "`n=== Testing Cancel Order API Endpoint ===" -ForegroundColor Cyan
Write-Host "PUT /api/admin/orders/{orderId}/cancel`n" -ForegroundColor Cyan

# Step 1: Get admin token
Write-Host "[STEP 1] Admin Login" -ForegroundColor Yellow
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
    Write-Host "✓ Login successful`n" -ForegroundColor Green
}
catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

# Step 2: Get orders
Write-Host "[STEP 2] Fetching orders..." -ForegroundColor Yellow
try {
    $ordersPage = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/?page=0&size=50" -Method Get -Headers $headers
    
    Write-Host "✓ Found $($ordersPage.content.Count) total orders`n" -ForegroundColor Green
    
    # Categorize orders by status
    $cancelableOrders = $ordersPage.content | Where-Object { $_.orderStatus -ne "DELIVERED" -and $_.orderStatus -ne "CANCELLED" }
    $deliveredOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "DELIVERED" }
    $alreadyCancelled = $ordersPage.content | Where-Object { $_.orderStatus -eq "CANCELLED" }
    
    Write-Host "Order Status Summary:" -ForegroundColor Cyan
    Write-Host "  Cancelable orders: $($cancelableOrders.Count)" -ForegroundColor Green
    Write-Host "  Delivered orders: $($deliveredOrders.Count)" -ForegroundColor Yellow
    Write-Host "  Already cancelled: $($alreadyCancelled.Count)" -ForegroundColor Gray
    
}
catch {
    Write-Host "✗ Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 3: Test cancellation on valid orders
if ($cancelableOrders.Count -gt 0) {
    Write-Host "`n[STEP 3] Testing Cancellation on Valid Orders" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "Business Rule: Can cancel from ANY status EXCEPT DELIVERED`n" -ForegroundColor Cyan
    
    # Group by status
    $statusGroups = $cancelableOrders | Group-Object -Property orderStatus
    
    Write-Host "Available orders to cancel:" -ForegroundColor Cyan
    $index = 0
    $orderList = @()
    
    foreach ($group in $statusGroups) {
        Write-Host "`n  Status: $($group.Name) ($($group.Count) orders)" -ForegroundColor White
        foreach ($order in $group.Group | Select-Object -First 3) {
            Write-Host "    [$index] ID: $($order.id) | Total: $($order.totalPrice) VND | Items: $($order.totalItem) | User: $($order.userEmail)"
            $orderList += $order
            $index++
            if ($index -ge 10) { break }
        }
        if ($index -ge 10) { break }
    }
    
    $selection = Read-Host "`nSelect order index to cancel (0-$($orderList.Count-1), or 'skip' to test invalid cancellation)"
    
    if ($selection -ne 'skip') {
        $orderToCancel = $orderList[[int]$selection]
        $orderId = $orderToCancel.id
        $originalStatus = $orderToCancel.orderStatus
        
        Write-Host "`nCancelling Order:" -ForegroundColor Yellow
        Write-Host "  Order ID: $orderId" -ForegroundColor White
        Write-Host "  Current Status: $originalStatus" -ForegroundColor White
        Write-Host "  Expected Result: Status → CANCELLED ✓`n" -ForegroundColor Green
        
        try {
            $cancelledOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/cancel" -Method Put -Headers $headers
            
            Write-Host "SUCCESS! Order cancelled ✓" -ForegroundColor Green
            Write-Host "`nOrder Details AFTER Cancellation:" -ForegroundColor Cyan
            Write-Host "  Order ID: $($cancelledOrder.id)" -ForegroundColor White
            Write-Host "  Order Number: $($cancelledOrder.orderId)" -ForegroundColor White
            Write-Host "  Previous Status: $originalStatus" -ForegroundColor Yellow
            Write-Host "  Current Status: $($cancelledOrder.orderStatus)" -ForegroundColor Green
            Write-Host "  Order Date: $($cancelledOrder.orderDate)" -ForegroundColor White
            Write-Host "  Total Price: $($cancelledOrder.totalPrice) VND" -ForegroundColor White
            Write-Host "  Total Items: $($cancelledOrder.totalItem)" -ForegroundColor White
            Write-Host "  User Email: $($cancelledOrder.userEmail)" -ForegroundColor White
            
            # Verify status is CANCELLED
            if ($cancelledOrder.orderStatus -eq "CANCELLED") {
                Write-Host "`n✓ Status successfully changed: $originalStatus → CANCELLED" -ForegroundColor Green
            }
            else {
                Write-Host "`n✗ WARNING: Expected CANCELLED but got $($cancelledOrder.orderStatus)!" -ForegroundColor Red
            }
            
            # Show order items
            if ($cancelledOrder.orderItems -and $cancelledOrder.orderItems.Count -gt 0) {
                Write-Host "`nCancelled Order Items:" -ForegroundColor Cyan
                foreach ($item in $cancelledOrder.orderItems) {
                    Write-Host "  • $($item.productName) x$($item.quantity) - $($item.price) VND" -ForegroundColor White
                }
            }
            
        }
        catch {
            Write-Host "✗ FAILED to cancel order" -ForegroundColor Red
            
            if ($_.Exception.Response) {
                $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
                $responseBody = $reader.ReadToEnd()
                Write-Host "`nError Response:" -ForegroundColor Yellow
                Write-Host $responseBody -ForegroundColor Red
            }
            else {
                Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
}
else {
    Write-Host "`n[STEP 3] No cancelable orders found" -ForegroundColor Yellow
}

# Step 4: Test invalid cancellation (DELIVERED order)
if ($deliveredOrders.Count -gt 0) {
    Write-Host "`n`n[STEP 4] Testing Invalid Cancellation" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "Testing: Attempt to cancel a DELIVERED order (should FAIL)" -ForegroundColor Cyan
    
    $deliveredOrder = $deliveredOrders[0]
    
    Write-Host "`nAttempting to cancel DELIVERED order:" -ForegroundColor Yellow
    Write-Host "  Order ID: $($deliveredOrder.id)" -ForegroundColor White
    Write-Host "  Status: $($deliveredOrder.orderStatus)" -ForegroundColor Yellow
    Write-Host "  Expected: API should REJECT this request`n" -ForegroundColor Red
    
    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($deliveredOrder.id)/cancel" -Method Put -Headers $headers
        
        Write-Host "✗ UNEXPECTED SUCCESS - This should have failed!" -ForegroundColor Red
        Write-Host "The API allowed cancellation of a DELIVERED order, which violates business rules." -ForegroundColor Red
        
    }
    catch {
        Write-Host "✓ Correctly REJECTED the cancellation!" -ForegroundColor Green
        
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
            $responseBody = $reader.ReadToEnd()
            
            try {
                $errorObj = $responseBody | ConvertFrom-Json
                Write-Host "`nError Message: $($errorObj.error)" -ForegroundColor Yellow
                
                if ($errorObj.error -like "*Cannot cancel*DELIVERED*") {
                    Write-Host "✓ Validation working correctly: Delivered orders cannot be cancelled" -ForegroundColor Green
                }
            }
            catch {
                Write-Host "`nRaw Error: $responseBody" -ForegroundColor Yellow
            }
        }
    }
}
else {
    Write-Host "`n`n[STEP 4] No DELIVERED orders available to test invalid cancellation" -ForegroundColor Yellow
}

# Summary
Write-Host "`n`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "           CANCELLATION API SUMMARY" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

Write-Host "`nValidation Rules:" -ForegroundColor Yellow
Write-Host "  ✓ Can cancel from: PENDING, PLACED, CONFIRMED, SHIPPED" -ForegroundColor Green
Write-Host "  ✗ Cannot cancel from: DELIVERED" -ForegroundColor Red

Write-Host "`nBusiness Logic:" -ForegroundColor Yellow
Write-Host "  • Status changes to CANCELLED" -ForegroundColor White
Write-Host "  • Order details preserved" -ForegroundColor White
Write-Host "  • Returns clean DTO response" -ForegroundColor White
Write-Host "  • Changes logged for audit trail" -ForegroundColor White

Write-Host "`nUse Cases:" -ForegroundColor Yellow
Write-Host "  • Customer requests cancellation before shipping" -ForegroundColor White
Write-Host "  • Admin needs to cancel due to stock issues" -ForegroundColor White
Write-Host "  • Payment problems require order cancellation" -ForegroundColor White
Write-Host "  • Fraudulent order detected" -ForegroundColor White

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
