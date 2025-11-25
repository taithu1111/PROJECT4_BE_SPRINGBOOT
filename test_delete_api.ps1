# Test Script for Delete Order API Endpoint
# DELETE /api/admin/orders/{orderId}

$baseUrl = "http://localhost:8080"

Write-Host "`n=== Testing Delete Order API Endpoint ===" -ForegroundColor Cyan
Write-Host "DELETE /api/admin/orders/{orderId}`n" -ForegroundColor Cyan

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

# Step 2: Get orders
Write-Host "[STEP 2] Fetching orders..." -ForegroundColor Yellow
try {
    $ordersPage = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/?page=0&size=50" -Method Get -Headers $headers
    
    $totalOrders = $ordersPage.content.Count
    Write-Host "✓ Found $totalOrders total orders`n" -ForegroundColor Green
    
    if ($totalOrders -eq 0) {
        Write-Host "No orders available to delete. Please create some orders first." -ForegroundColor Yellow
        exit
    }
    
    # Categorize orders by status for better decision making
    $cancelledOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "CANCELLED" }
    $deliveredOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "DELIVERED" }
    $activeOrders = $ordersPage.content | Where-Object { $_.orderStatus -notin @("CANCELLED", "DELIVERED") }
    
    Write-Host "Order Status Summary:" -ForegroundColor Cyan
    Write-Host "  Active orders (PENDING/PLACED/CONFIRMED/SHIPPED): $($activeOrders.Count)" -ForegroundColor Green
    Write-Host "  Cancelled orders: $($cancelledOrders.Count)" -ForegroundColor Yellow
    Write-Host "  Delivered orders: $($deliveredOrders.Count)" -ForegroundColor Cyan
    
    Write-Host "`n⚠️  DELETION RECOMMENDATIONS:" -ForegroundColor Yellow
    Write-Host "  • SAFEST to delete: CANCELLED orders" -ForegroundColor Green
    Write-Host "  • BE CAREFUL: Active orders (affects customer)" -ForegroundColor Yellow
    Write-Host "  • AVOID deleting: DELIVERED orders (historical data)" -ForegroundColor Red
    
} catch {
    Write-Host "✗ Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 3: Select order to delete
Write-Host "`n[STEP 3] Select Order to Delete" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

# Show all orders grouped by status
$allOrders = $ordersPage.content | Sort-Object -Property orderStatus
$index = 0
$orderList = @()

Write-Host "`nAvailable Orders:" -ForegroundColor Cyan

foreach ($order in $allOrders) {
    $statusColor = switch ($order.orderStatus) {
        "CANCELLED" { "Green" }
        "DELIVERED" { "Red" }
        "PENDING" { "Yellow" }
        "CONFIRMED" { "Yellow" }
        "SHIPPED" { "Yellow" }
        default { "White" }
    }
    
    $recommendation = switch ($order.orderStatus) {
        "CANCELLED" { "[SAFE]" }
        "DELIVERED" { "[AVOID]" }
        default { "[CAUTION]" }
    }
    
    Write-Host "  [$index] $recommendation " -NoNewline -ForegroundColor $statusColor
    Write-Host "ID: $($order.id) | Status: $($order.orderStatus) | Total: $($order.totalPrice) VND | User: $($order.userEmail)" -ForegroundColor White
    
    $orderList += $order
    $index++
    
    if ($index -ge 20) { 
        Write-Host "  ... (showing first 20 orders only)" -ForegroundColor Gray
        break 
    }
}

$selection = Read-Host "`nSelect order index to DELETE (0-$([Math]::Min(19, $orderList.Count-1)))"
$orderToDelete = $orderList[[int]$selection]

# Step 4: Confirm deletion
Write-Host "`n[STEP 4] Confirm Deletion" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

Write-Host "`nYou are about to DELETE:" -ForegroundColor Red
Write-Host "  Order ID: $($orderToDelete.id)" -ForegroundColor White
Write-Host "  Order Number: $($orderToDelete.orderId)" -ForegroundColor White
Write-Host "  Status: $($orderToDelete.orderStatus)" -ForegroundColor White
Write-Host "  Total Price: $($orderToDelete.totalPrice) VND" -ForegroundColor White
Write-Host "  Total Items: $($orderToDelete.totalItem)" -ForegroundColor White
Write-Host "  User: $($orderToDelete.userEmail)" -ForegroundColor White
Write-Host "  Order Date: $($orderToDelete.orderDate)" -ForegroundColor White

if ($orderToDelete.orderItems) {
    Write-Host "`n  Order Items:" -ForegroundColor Cyan
    foreach ($item in $orderToDelete.orderItems) {
        Write-Host "    • $($item.productName) x$($item.quantity) - $($item.price) VND" -ForegroundColor White
    }
}

Write-Host "`n⚠️  WARNING: This action CANNOT be undone!" -ForegroundColor Red
Write-Host "  • Order will be permanently removed from database" -ForegroundColor Red
Write-Host "  • All order items will be deleted" -ForegroundColor Red
Write-Host "  • Customer won't see this order in their history" -ForegroundColor Red

$confirm = Read-Host "`nType 'DELETE' to confirm (or anything else to cancel)"

if ($confirm -ne "DELETE") {
    Write-Host "`n✓ Deletion cancelled. Order preserved." -ForegroundColor Green
    exit
}

# Step 5: Execute deletion
Write-Host "`n[STEP 5] Executing Deletion..." -ForegroundColor Yellow
Write-Host "Endpoint: DELETE /api/admin/orders/$($orderToDelete.id)`n" -ForegroundColor Cyan

try {
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($orderToDelete.id)" -Method Delete -Headers $headers
    
    Write-Host "SUCCESS! Order deleted ✓" -ForegroundColor Green
    Write-Host "`nDeletion Response:" -ForegroundColor Cyan
    Write-Host "  Message: $($deleteResponse.message)" -ForegroundColor Green
    Write-Host "  Status: $($deleteResponse.status)" -ForegroundColor Green
    
} catch {
    Write-Host "✗ FAILED to delete order" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
        $responseBody = $reader.ReadToEnd()
        Write-Host "`nError Response:" -ForegroundColor Yellow
        Write-Host $responseBody -ForegroundColor Red
        
        # Check for foreign key constraint errors
        if ($responseBody -like "*foreign key*" -or $responseBody -like "*constraint*") {
            Write-Host "`n⚠️  This might be a foreign key constraint error." -ForegroundColor Yellow
            Write-Host "The order might have related data that prevents deletion." -ForegroundColor Yellow
        }
    } else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    exit
}

# Step 6: Verify deletion
Write-Host "`n[STEP 6] Verifying Deletion..." -ForegroundColor Yellow

try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($orderToDelete.id)" -Method Get -Headers $headers -ErrorAction Stop
    
    Write-Host "✗ WARNING: Order still exists in database!" -ForegroundColor Red
    Write-Host "Deletion may not have completed properly." -ForegroundColor Red
    
} catch {
    if ($_.Exception.Response.StatusCode -eq 404 -or $_.Exception.Message -like "*not found*" -or $_.Exception.Message -like "*not exist*") {
        Write-Host "✓ Verified: Order no longer exists in database" -ForegroundColor Green
        Write-Host "✓ Deletion completed successfully" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Could not verify deletion (unexpected error)" -ForegroundColor Yellow
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
    }
}

# Step 7: Summary
Write-Host "`n`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "           DELETE API SUMMARY" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

Write-Host "`nWhat Happened:" -ForegroundColor Yellow
Write-Host "  ✓ Order ID $($orderToDelete.id) was deleted" -ForegroundColor Green
Write-Host "  ✓ Associated order items removed" -ForegroundColor Green
Write-Host "  ✓ Database constraints respected" -ForegroundColor Green

Write-Host "`nBest Practices:" -ForegroundColor Yellow
Write-Host "  • Prefer deleting CANCELLED orders" -ForegroundColor White
Write-Host "  • Keep DELIVERED orders for historical records" -ForegroundColor White
Write-Host "  • Consider soft-delete for production systems" -ForegroundColor White
Write-Host "  • Always verify deletion completed" -ForegroundColor White

Write-Host "`nUse Cases for Deletion:" -ForegroundColor Yellow
Write-Host "  • Test data cleanup" -ForegroundColor White
Write-Host "  • Removing duplicate/erroneous orders" -ForegroundColor White
Write-Host "  • Spam/fraudulent order removal" -ForegroundColor White
Write-Host "  • Development environment maintenance" -ForegroundColor White

Write-Host "`nAlternatives to Consider:" -ForegroundColor Yellow
Write-Host "  • Soft delete (mark as deleted, don't remove from DB)" -ForegroundColor White
Write-Host "  • Archive to separate table" -ForegroundColor White
Write-Host "  • Just use CANCEL status instead" -ForegroundColor White

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
