package com.example.sftpuploader.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

@Service
public class SftpService {
    private static final Logger logger = LoggerFactory.getLogger(SftpService.class);

    @Value("${sftp.host:localhost}")
    private String defaultHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.username:root}")
    private String defaultUsername;

    @Value("${sftp.password:}")
    private String defaultPassword;

    @Value("${sftp.upload-dir:/upload}")
    private String defaultUploadDir;

    @Value("${sftp.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${sftp.channel-timeout:10000}")
    private int channelTimeout;

    @Value("${sftp.strict-host-checking:false}")
    private boolean strictHostChecking;

    /**
     * Stream-based upload for handling large files.
     */
    public void upload(String host, String username, String password, String remoteDir, String filename, InputStream dataStream) throws Exception {
        String effectiveHost = (host != null && !host.isEmpty()) ? host : defaultHost;
        String effectiveUsername = (username != null && !username.isEmpty()) ? username : defaultUsername;
        String effectivePassword = (password != null && !password.isEmpty()) ? password : defaultPassword;
        String effectiveRemoteDir = (remoteDir != null && !remoteDir.isEmpty()) ? remoteDir : defaultUploadDir;

        logger.info("Starting large file upload to SFTP - File: {}", filename);
        logger.info("Connecting to SFTP server: {}:{}", effectiveHost, sftpPort);
        
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(effectiveUsername, effectiveHost, sftpPort);
            session.setPassword(effectivePassword);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", strictHostChecking ? "yes" : "no");
            session.setConfig(config);
            session.connect(connectionTimeout);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(channelTimeout);

            try {
                channel.cd(effectiveRemoteDir);
            } catch (Exception e) {
                throw new IllegalArgumentException("Remote directory does not exist: " + effectiveRemoteDir);
            }

            logger.info("Starting file transfer to: {}/{}", effectiveRemoteDir, filename);
            TransferProgressMonitor progressMonitor = new TransferProgressMonitor(filename);
            channel.put(dataStream, filename, progressMonitor);
            logger.info("File transfer completed, setting permissions for: {}", filename);
            channel.chmod(0777, filename);
            logger.info("Upload completed successfully for file: {}", filename);
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    /**
     * Legacy byte array-based upload, delegates to streaming version.
     */
    public void upload(String host, String username, String password, String remoteDir, String filename, byte[] data) throws Exception {
        try (InputStream in = new ByteArrayInputStream(data)) {
            upload(host, username, password, remoteDir, filename, in);
        }
    }
}
