package com.example.sftpuploader.web;

public class UploadResponse {
    private String message;
    private String filename;
    private String uploadPath;

    public UploadResponse(String message, String filename, String uploadPath) {
        this.message = message;
        this.filename = filename;
        this.uploadPath = uploadPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }
}