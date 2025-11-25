$baseUrl = "http://localhost:8080"
$email = "nguyenphanbaolong1402@gmail.com"
$password = "123456" # Assuming this is the password, if not, we might need to ask user

function Test-Endpoint {
    param (
        [string]$Method,
        [string]$Url,
        [string]$Token,
        [hashtable]$Body
    )

    $headers = @{
        "Content-Type" = "application/json"
    }
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $headers
            ErrorAction = "Stop"
        }
        if ($Body) {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
        }

        $response = Invoke-RestMethod @params
        Write-Host "[$Method] $Url - SUCCESS" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "[$Method] $Url - FAILED" -ForegroundColor Red
        Write-Host $_.Exception.Message
        if ($_.Exception.Response) {
             $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
             $responseBody = $reader.ReadToEnd()
             Write-Host "Response Body: $responseBody"
        }
        return $null
    }
}

# 1. Login as Admin
Write-Host "`n--- Step 1: Login as Admin ---"
$loginBody = @{
    email = $email
    password = $password
}
$loginResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/auth/signin" -Body $loginBody

if (-not $loginResponse -or -not $loginResponse.jwt) {
    Write-Host "Failed to login as admin. Please check credentials." -ForegroundColor Red
    exit
}

$token = $loginResponse.jwt
Write-Host "Got Token: $token"

# 2. Create Product
Write-Host "`n--- Step 2: Create Product ---"
$productBody = @{
    title = "Test Product $(Get-Date -Format 'yyyyMMddHHmmss')"
    description = "Test Description"
    price = 100
    quantity = 10
    brand = "Test Brand"
    firstLevelCategory = "Men"
    secondLevelCategory = "Clothing"
    colors = @(@{name="Red"})
    images = @(@{imageUrl="http://example.com/image.png"})
}
$product = Test-Endpoint -Method "POST" -Url "$baseUrl/api/admin/products/" -Token $token -Body $productBody

if (-not $product) {
    Write-Host "Failed to create product." -ForegroundColor Red
    exit
}
$productId = $product.id
Write-Host "Created Product ID: $productId"

# 3. Update Product
Write-Host "`n--- Step 3: Update Product ---"
$updateBody = $productBody.Clone()
$updateBody.title = "Updated Product Title"
$updatedProduct = Test-Endpoint -Method "PUT" -Url "$baseUrl/api/admin/products/$productId" -Token $token -Body $updateBody

if ($updatedProduct.title -eq "Updated Product Title") {
    Write-Host "Product Updated Successfully" -ForegroundColor Green
} else {
    Write-Host "Product Update Verification Failed" -ForegroundColor Red
}

# 4. Get All Orders (Pagination)
Write-Host "`n--- Step 4: Get All Orders (Pagination) ---"
$ordersPage = Test-Endpoint -Method "GET" -Url "$baseUrl/api/admin/orders/?page=0&size=5&sortBy=createAt" -Token $token

if ($ordersPage -and $ordersPage.content) {
    Write-Host "Got $($ordersPage.content.Count) orders on page 0" -ForegroundColor Green
} else {
    Write-Host "Failed to get orders or no orders found." -ForegroundColor Yellow
}

# 5. Order Status Flow (Requires an existing order ID, creating one is complex as it requires user login and cart)
# We will try to pick the first order from the list if available
if ($ordersPage.content.Count -gt 0) {
    $orderId = $ordersPage.content[0].id
    Write-Host "Testing Order Status Flow on Order ID: $orderId"

    # Note: This might fail if the order is not in the correct initial state.
    # Ideally we should create a fresh order, but for now let's try to confirm it.
    
    # Try Confirm
    Write-Host "Attempting to Confirm Order..."
    Test-Endpoint -Method "PUT" -Url "$baseUrl/api/admin/orders/$orderId/confirmed" -Token $token

    # Try Ship
    Write-Host "Attempting to Ship Order..."
    Test-Endpoint -Method "PUT" -Url "$baseUrl/api/admin/orders/$orderId/ship" -Token $token

    # Try Deliver
    Write-Host "Attempting to Deliver Order..."
    Test-Endpoint -Method "PUT" -Url "$baseUrl/api/admin/orders/$orderId/deliver" -Token $token

    # Verify Delivery Date
    $deliveredOrder = Test-Endpoint -Method "GET" -Url "$baseUrl/api/orders/$orderId" -Token $token
    if ($deliveredOrder.deliveryDate) {
        Write-Host "Delivery Date is set: $($deliveredOrder.deliveryDate)" -ForegroundColor Green
    } else {
        Write-Host "Delivery Date is NOT set!" -ForegroundColor Red
    }
} else {
    Write-Host "No orders found to test status flow." -ForegroundColor Yellow
}

# 6. Delete Product (Check Constraint)
Write-Host "`n--- Step 6: Delete Product ---"
# This should succeed as we just created it and it's not in any order
Test-Endpoint -Method "DELETE" -Url "$baseUrl/api/admin/products/$productId" -Token $token
