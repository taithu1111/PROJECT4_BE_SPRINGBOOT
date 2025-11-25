$baseUrl = "http://localhost:8080"
$email = "tempadmin@example.com"
$password = "password"

$body = @{
    email = $email
    password = $password
    firstName = "Temp"
    lastName = "Admin"
    mobile = "1234567890"
}

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/signup" -Method Post -Body ($body | ConvertTo-Json) -ContentType "application/json"
    Write-Host "Signup Successful: $($response.message)" -ForegroundColor Green
    Write-Host "Token: $($response.token)"
} catch {
    Write-Host "Signup Failed" -ForegroundColor Red
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
         $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
         $responseBody = $reader.ReadToEnd()
         Write-Host "Response Body: $responseBody"
    }
}
