# Test Script for Ship API Endpoint
# PUT /api/admin/orders/{orderId}/ship

$baseUrl = "http://localhost:8080"

# STEP 1: Get your admin token
Write-Host "`n=== Testing Ship API Endpoint ===" -ForegroundColor Cyan
Write-Host "`nSTEP 1: Login as Admin" -ForegroundColor Yellow
Write-Host "Endpoint: POST /auth/signin"
Write-Host "Body: { email: 'your-admin-email', password: 'your-password' }"
Write-Host "-> Copy the 'jwt' from response`n"

$adminToken = Read-Host "Paste your admin JWT token here"

if (-not $adminToken) {
    Write-Host "No token provided. Exiting." -ForegroundColor Red
    exit
}

# STEP 2: Get an order to test
Write-Host "`nSTEP 2: Fetching orders..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type"  = "application/json"
}

try {
    $orders = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/?page=0&size=10" -Method Get -Headers $headers
    
    if ($orders.content.Count -eq 0) {
        Write-Host "No orders found. Please create an order first." -ForegroundColor Red
        exit
    }
    
    Write-Host "Found $($orders.content.Count) orders:" -ForegroundColor Green
    for ($i = 0; $i -lt $orders.content.Count; $i++) {
        $order = $orders.content[$i]
        Write-Host "  [$i] Order ID: $($order.id) - Status: $($order.orderStatus)"
    }
    
    $orderIndex = Read-Host "`nSelect order index to test Ship API (0-$($orders.content.Count-1))"
    $selectedOrder = $orders.content[[int]$orderIndex]
    $orderId = $selectedOrder.id
    $currentStatus = $selectedOrder.orderStatus
    
    Write-Host "`nSelected Order ID: $orderId - Current Status: $currentStatus" -ForegroundColor Cyan
    
}
catch {
    Write-Host "Failed to fetch orders: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# STEP 3: Test Ship API
Write-Host "`nSTEP 3: Testing Ship API..." -ForegroundColor Yellow
Write-Host "Endpoint: PUT /api/admin/orders/$orderId/ship"
Write-Host "`nBusiness Rule: Can only ship CONFIRMED orders"
Write-Host "Current Status: $currentStatus`n"

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/orders/$orderId/ship" -Method Put -Headers $headers
    
    Write-Host "SUCCESS! Order shipped" -ForegroundColor Green
    Write-Host "Order ID: $($response.id)"
    Write-Host "New Status: $($response.orderStatus)"
    Write-Host "Order Date: $($response.orderDate)"
    Write-Host "`nStatus transition: $currentStatus -> $($response.orderStatus)" -ForegroundColor Cyan
    
}
catch {
    Write-Host "FAILED to ship order" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error: $responseBody" -ForegroundColor Yellow
        
        if ($currentStatus -ne "CONFIRMED") {
            Write-Host "`nREASON: Order status is '$currentStatus', but Ship API requires status to be 'CONFIRMED'" -ForegroundColor Yellow
            Write-Host "`nTo test successfully:" -ForegroundColor Cyan
            Write-Host "1. First confirm the order: PUT /api/admin/orders/$orderId/confirmed"
            Write-Host "2. Then ship the order: PUT /api/admin/orders/$orderId/ship"
        }
    }
    else {
        Write-Host "Error: $($_.Exception.Message)"
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
