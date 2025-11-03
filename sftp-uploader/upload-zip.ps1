# API endpoint
$uri = "http://localhost:8080/api/v1/sftp/upload/file"

# Create form data
$form = @{
    file = Get-Item -Path "test.zip"
    remoteDir = "/upload"
}

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Form $form
    Write-Host "ZIP file uploaded successfully!"
    Write-Host $response | ConvertTo-Json
} catch {
    Write-Host "Error uploading ZIP file: $_"
    Write-Host $_.Exception.Response.StatusCode
}