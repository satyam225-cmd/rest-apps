# SFTP File Uploader

A Spring Boot application that provides REST endpoints for uploading files to an SFTP server and listing remote directory contents.

## Features

- Upload large files (up to 3GB) to SFTP server
- Stream-based upload to minimize memory usage
- Detailed transfer progress monitoring
- Remote directory listing (ls -ltar style)
- OpenAPI/Swagger documentation
- Configurable SFTP connection settings

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- An SFTP server

## Configuration

Configuration is in `application.yaml`. Default values shown below:

```yaml
sftp:
  host: localhost        # SFTP server hostname
  port: 2222            # SFTP port
  username: root        # SFTP username
  password: password    # SFTP password
  upload-dir: /upload   # Default remote directory
  connection-timeout: 10000
  channel-timeout: 10000
  strict-host-checking: false

spring:
  servlet:
    multipart:
      max-file-size: 3GB
      max-request-size: 3GB
```

## Building

```bash
mvn clean package
```

## Running

```bash
mvn spring-boot:run
```

Or run the jar directly:

```bash
java -jar target/sftp-uploader-0.1.0.jar
```

## API Documentation

Once running, view the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## REST Endpoints

### Upload File

Upload a file to the SFTP server.

```http
POST /api/upload
Content-Type: multipart/form-data

Parameters:
- file: The file to upload (required)
- host: SFTP host (optional, default from application.yaml)
- port: SFTP port (optional, default from application.yaml)
- username: SFTP username (optional, default from application.yaml)
- password: SFTP password (optional, default from application.yaml)
- remoteDir: Target directory on SFTP server (optional, default from application.yaml)
```

Example using curl:
```bash
# Using configured defaults
curl -X POST "http://localhost:8080/api/upload" \
  -F "file=@local-file.zip"

# Override defaults
curl -X POST "http://localhost:8080/api/upload" \
  -F "file=@local-file.zip" \
  -F "remoteDir=/upload/folder" \
  -F "host=sftp.example.com"
```

### List Remote Directory

Get a directory listing from the SFTP server in ls -ltar style format.

```http
GET /api/sftp/list

Parameters:
- host: SFTP host (optional, default from application.yaml)
- port: SFTP port (optional, default from application.yaml)
- username: SFTP username (optional, default from application.yaml)
- password: SFTP password (optional, default from application.yaml)
- remoteDir: Directory to list (required)
```

Example using curl:
```bash
# Using configured defaults
curl "http://localhost:8080/api/sftp/list?remoteDir=/upload"

# Override defaults
curl "http://localhost:8080/api/sftp/list?host=sftp.example.com&port=22&username=user&password=pass&remoteDir=/upload"
```

Response format:
```json
[
  {
    "perms": "-rw-r--r--",
    "uid": 1000,
    "gid": 1000,
    "size": 1234567,
    "date": "Nov 02 15:30",
    "name": "example.zip"
  }
]
```

## Security Notes

1. In production environments:
   - Use HTTPS to secure API communication
   - Store SFTP credentials securely (e.g., vault, environment variables)
   - Consider using SSH keys instead of passwords
   - Add authentication/authorization to the API endpoints

2. The current implementation:
   - Accepts credentials as query parameters (convenient but not secure)
   - Allows directory listing without authentication
   - Logs transfer progress (may include sensitive information)

## Monitoring and Logging

- File transfer progress is logged using SLF4J
- Each upload shows:
  - Start of transfer with file size
  - Progress updates every second
  - Transfer speed in MB/s
  - Estimated time remaining
  - Completion status with average speed

Example log output:
```
Starting transfer of example.zip - Total size: 2.5 GB
example.zip - Progress: 25.5% (650.2 MB/2.5 GB) - Speed: 45.2 MB/s - ETA: 42.3 seconds
Transfer completed for example.zip - Total: 2.5 GB - Avg Speed: 44.5 MB/s - Duration: 58.2 seconds
```

## Error Handling

Common errors and their causes:
- 400 Bad Request: Missing required parameters or invalid input
- 404 Not Found: Remote directory doesn't exist
- 500 Internal Server Error: SFTP connection failed or transfer error

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the LICENSE file for details.
