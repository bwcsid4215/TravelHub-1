package com.bwc.travel_request_management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileStorageProperties {
    private String uploadDir = "uploads/travel-documents";
    private long maxFileSize = 10485760; // 10MB
    private String[] allowedTypes = {"pdf", "jpg", "jpeg", "png", "gif", "doc", "docx"};
}