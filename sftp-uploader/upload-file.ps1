# File path to upload
$filePath = "C:\dev\upload-files\test.txt"

# API endpoint
$uri = "http://localhost:8080/api/v1/sftp/upload/file"

# Create form data with proper multipart/form-data content
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"
$fileName = Split-Path $filePath -Leaf

$bodyLines = (
    "--$boundary",
    "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
    "Content-Type: application/octet-stream$LF",
    [System.IO.File]::ReadAllBytes($filePath),
    "$LF--$boundary",
    "Content-Disposition: form-data; name=`"remoteDir`"$LF",
    "/upload",
    "--$boundary--"
)

# Convert to bytes
$body = [System.Text.Encoding]::UTF8.GetBytes(($bodyLines | ForEach-Object { 
    if ($_ -is [byte[]]) { $_ } 
    else { [System.Text.Encoding]::UTF8.GetBytes($_ + $LF) }
}) -join '')

# Send request
try {
    $response = Invoke-WebRequest -Uri $uri `
        -Method Post `
        -ContentType "multipart/form-data; boundary=$boundary" `
        -Body $body

    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Response: $($response.Content)"
} catch {
    Write-Host "Error uploading file: $_"
    Write-Host $_.Exception.Response.StatusCode
}