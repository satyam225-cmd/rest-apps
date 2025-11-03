package com.example.sftpuploader.controller;

import com.example.sftpuploader.service.SftpListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sftp")
public class SftpListingController {

    private final SftpListingService listingService;
    
    @org.springframework.beans.factory.annotation.Value("${sftp.host:localhost}")
    private String defaultHost;

    @org.springframework.beans.factory.annotation.Value("${sftp.port:22}")
    private int defaultPort;

    @org.springframework.beans.factory.annotation.Value("${sftp.username:}")
    private String defaultUsername;

    @org.springframework.beans.factory.annotation.Value("${sftp.password:}")
    private String defaultPassword;

    public SftpListingController(SftpListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * GET /api/sftp/list
     * Returns a JSON array of directory entries similar to "ls -ltar" output.
     */
    @Operation(summary = "List remote SFTP directory", description = "Returns a JSON listing of files in the given remote SFTP directory.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing returned"),
            @ApiResponse(responseCode = "400", description = "Bad request - missing/invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Server error or SFTP connection failure")
    })
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> list(
            @Parameter(description = "SFTP host or IP (optional, default from application.yaml)") @RequestParam(required = false) String host,
            @Parameter(description = "SFTP port (optional, default from application.yaml)") @RequestParam(required = false) Integer port,
            @Parameter(description = "SFTP username (optional, default from application.yaml)") @RequestParam(required = false) String username,
            @Parameter(description = "SFTP password (optional, default from application.yaml) (use POST/secure storage in production)") @RequestParam(required = false) String password,
            @Parameter(description = "Remote directory to list", required = true) @RequestParam String remoteDir
    ) throws Exception {
        String effectiveHost = (host == null || host.isEmpty()) ? defaultHost : host;
        int effectivePort = (port == null) ? defaultPort : port;
        String effectiveUser = (username == null || username.isEmpty()) ? defaultUsername : username;
        String effectivePassword = (password == null || password.isEmpty()) ? defaultPassword : password;

        List<Map<String, Object>> entries = listingService.listDirectory(effectiveHost, effectivePort, effectiveUser, effectivePassword, remoteDir);
        return ResponseEntity.ok(entries);
    }
}
