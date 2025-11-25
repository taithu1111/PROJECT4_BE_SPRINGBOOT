# Test Script for Deliver API Endpoint
# PUT /api/admin/orders/{orderId}/deliver

$baseUrl = "http://localhost:8080"

Write-Host "`n=== Testing Deliver API Endpoint ===" -ForegroundColor Cyan
Write-Host "PUT /api/admin/orders/{orderId}/deliver`n" -ForegroundColor Cyan

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

# Step 2: Get orders and find a SHIPPED order
Write-Host "[STEP 2] Finding SHIPPED orders..." -ForegroundColor Yellow
try {
    $ordersPage = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/?page=0&size=20" -Method Get -Headers $headers
    
    $shippedOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "SHIPPED" }
    
    if ($shippedOrders.Count -eq 0) {
        Write-Host "No SHIPPED orders found. Let me help you create one...`n" -ForegroundColor Yellow
        
        # Find a CONFIRMED order to ship first
        $confirmedOrders = $ordersPage.content | Where-Object { $_.orderStatus -eq "CONFIRMED" }
        
        if ($confirmedOrders.Count -eq 0) {
            Write-Host "No CONFIRMED orders found either. You need to:" -ForegroundColor Red
            Write-Host "1. Create an order (as a user)" -ForegroundColor Red
            Write-Host "2. Confirm it: PUT /api/admin/orders/{orderId}/confirmed" -ForegroundColor Red
            Write-Host "3. Ship it: PUT /api/admin/orders/{orderId}/ship" -ForegroundColor Red
            Write-Host "4. Then deliver it`n" -ForegroundColor Red
            exit
        }
        
        # Ship a CONFIRMED order
        $orderToShip = $confirmedOrders[0]
        Write-Host "  • Shipping order ID: $($orderToShip.id) (currently $($orderToShip.orderStatus))" -ForegroundColor Yellow
        
        $shippedOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$($orderToShip.id)/ship" -Method Put -Headers $headers
        Write-Host "  ✓ Order shipped successfully`n" -ForegroundColor Green
        
        $orderId = $shippedOrder.id
    }
    else {
        Write-Host "Found $($shippedOrders.Count) SHIPPED order(s)" -ForegroundColor Green
        
        # Show available SHIPPED orders
        Write-Host "`nAvailable SHIPPED orders:" -ForegroundColor Cyan
        for ($i = 0; $i -lt $shippedOrders.Count; $i++) {
            $order = $shippedOrders[$i]
            Write-Host "  [$i] Order ID: $($order.id) | Total: $($order.totalPrice) | Items: $($order.totalItem) | Date: $($order.orderDate)"
        }
        
        $selection = Read-Host "`nSelect order index to deliver (0-$($shippedOrders.Count-1))"
        $orderId = $shippedOrders[[int]$selection].id
    }
}
catch {
    Write-Host "✗ Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 3: Deliver the order
Write-Host "`n[STEP 3] Testing Deliver API" -ForegroundColor Yellow
Write-Host "Endpoint: PUT /api/admin/orders/$orderId/deliver" -ForegroundColor Cyan
Write-Host "Business Rule: Can only deliver SHIPPED orders" -ForegroundColor Cyan
Write-Host "Expected: Status → DELIVERED, deliveryDate set`n" -ForegroundColor Cyan

try {
    $deliveredOrder = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/deliver" -Method Put -Headers $headers
    
    Write-Host "SUCCESS! Order delivered ✓" -ForegroundColor Green
    Write-Host "`nOrder Details:" -ForegroundColor Cyan
    Write-Host "  Order ID: $($deliveredOrder.id)" -ForegroundColor White
    Write-Host "  Order Number: $($deliveredOrder.orderId)" -ForegroundColor White
    Write-Host "  Status: $($deliveredOrder.orderStatus)" -ForegroundColor Green
    Write-Host "  Order Date: $($deliveredOrder.orderDate)" -ForegroundColor White
    Write-Host "  Delivery Date: $($deliveredOrder.deliveryDate)" -ForegroundColor Green
    Write-Host "  Total Price: $($deliveredOrder.totalPrice)" -ForegroundColor White
    Write-Host "  Total Items: $($deliveredOrder.totalItem)" -ForegroundColor White
    Write-Host "  User Email: $($deliveredOrder.userEmail)" -ForegroundColor White
    
    # Verify deliveryDate is set
    if ($deliveredOrder.deliveryDate) {
        Write-Host "`n✓ Delivery date successfully set!" -ForegroundColor Green
    }
    else {
        Write-Host "`n✗ WARNING: Delivery date not set!" -ForegroundColor Red
    }
    
    # Show shipping address
    if ($deliveredOrder.shippingAddress) {
        Write-Host "`nShipping Address:" -ForegroundColor Cyan
        Write-Host "  Street: $($deliveredOrder.shippingAddress.streetAddress)" -ForegroundColor White
        Write-Host "  City: $($deliveredOrder.shippingAddress.city)" -ForegroundColor White
        Write-Host "  Zip Code: $($deliveredOrder.shippingAddress.zipCode)" -ForegroundColor White
    }
    
    # Show order items
    if ($deliveredOrder.orderItems) {
        Write-Host "`nOrder Items:" -ForegroundColor Cyan
        foreach ($item in $deliveredOrder.orderItems) {
            Write-Host "  • $($item.productName) x$($item.quantity) - $($item.price) VND" -ForegroundColor White
        }
    }
    
}
catch {
    Write-Host "FAILED to deliver order" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
        $responseBody = $reader.ReadToEnd()
        Write-Host "`nError Response:" -ForegroundColor Yellow
        Write-Host $responseBody -ForegroundColor Red
        
        # Parse error message
        try {
            $errorObj = $responseBody | ConvertFrom-Json
            if ($errorObj.error -like "*Cannot deliver*") {
                Write-Host "`nREASON: The order is not in SHIPPED status" -ForegroundColor Yellow
                Write-Host "Current workflow requires:" -ForegroundColor Yellow
                Write-Host "  1. PENDING → CONFIRMED (PUT /api/admin/orders/{id}/confirmed)" -ForegroundColor Yellow
                Write-Host "  2. CONFIRMED → SHIPPED (PUT /api/admin/orders/{id}/ship)" -ForegroundColor Yellow
                Write-Host "  3. SHIPPED → DELIVERED (PUT /api/admin/orders/{id}/deliver)" -ForegroundColor Yellow
            }
        }
        catch {
            # Ignore JSON parse errors
        }
    }
    else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
