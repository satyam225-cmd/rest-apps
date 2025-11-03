$headers = @{
    'Content-Type' = 'application/json'
}

$body = Get-Content -Raw -Path "test-upload.json"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/upload/text" `
                                -Method Post `
                                -Headers $headers `
                                -Body $body `
                                -ContentType "application/json"
    Write-Host "Success: $response"
} catch {
    Write-Host "Error: $_"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Status Description: $($_.Exception.Response.StatusDescription)"
}