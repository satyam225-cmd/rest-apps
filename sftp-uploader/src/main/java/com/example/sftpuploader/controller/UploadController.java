package com.example.sftpuploader.controller;

import com.example.sftpuploader.service.SftpService;
import com.example.sftpuploader.web.ErrorResponse;
import com.example.sftpuploader.web.UploadResponse;
import com.example.sftpuploader.web.UploadTextRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/sftp")
@Tag(name = "SFTP Upload", description = "API endpoints for uploading files via SFTP")
public class UploadController {

    private final SftpService sftpService;

    public UploadController(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    @Operation(summary = "Upload a text file", description = "Upload a text file to SFTP server using JSON payload")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully",
            content = @Content(schema = @Schema(implementation = UploadResponse.class)))
    @ApiResponse(responseCode = "500", description = "Server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/upload/text")
    public ResponseEntity<?> uploadText(@RequestBody UploadTextRequest req) {
        try {
            byte[] data = req.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            sftpService.upload(req.getHost(), req.getUsername(), req.getPassword(), req.getRemoteDir(), req.getFilename(), data);
            return ResponseEntity.ok(new UploadResponse("File uploaded successfully", req.getFilename(), req.getRemoteDir()));
        } catch (IllegalArgumentException e) {
            // client error: requested remote directory doesn't exist
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Upload a binary file", description = "Upload any binary file (including ZIP files) to SFTP server using multipart/form-data")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully",
            content = @Content(schema = @Schema(implementation = UploadResponse.class)))
    @ApiResponse(responseCode = "500", description = "Server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "SFTP host address") @RequestParam(value = "host", required = false) String host,
            @Parameter(description = "SFTP username") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "SFTP password") @RequestParam(value = "password", required = false) String password,
            @Parameter(description = "Remote directory path") @RequestParam(value = "remoteDir", required = false) String remoteDir,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String filename = originalFilename != null ? new java.io.File(originalFilename).getName() : "unknown";
            // Use streaming to handle large files
            try (InputStream fileStream = file.getInputStream()) {
                sftpService.upload(host, username, password, remoteDir, filename, fileStream);
            }
            return ResponseEntity.ok(new UploadResponse("File uploaded successfully", filename, remoteDir));
        } catch (IllegalArgumentException e) {
            // client requested a remote directory that doesn't exist
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error reading file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error uploading file: " + e.getMessage()));
        }
    }
}
