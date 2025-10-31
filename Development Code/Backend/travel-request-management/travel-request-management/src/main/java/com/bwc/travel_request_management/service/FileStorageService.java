package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.config.FileStorageProperties;
import com.bwc.travel_request_management.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, UUID travelRequestId, String documentType) {
        // Validate file
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        // Check file size
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new FileStorageException("File size exceeds maximum limit of 10MB.");
        }

        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = generateFileName(travelRequestId, documentType, fileExtension);

        try {
            // Check for invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Create request-specific directory
            Path requestDirectory = this.fileStorageLocation.resolve(travelRequestId.toString());
            Files.createDirectories(requestDirectory);

            // Copy file to the target location
            Path targetLocation = requestDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", targetLocation);
            return fileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            // FIXED: Look for file in ALL subdirectories recursively
            Path filePath = findFileInSubdirectories(fileName);
            
            if (filePath != null && Files.exists(filePath)) {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists()) {
                    return resource;
                }
            }
            
            throw new FileStorageException("File not found " + fileName);
            
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        } catch (IOException ex) {
            throw new FileStorageException("Error accessing file " + fileName, ex);
        }
    }

    /**
     * FIXED: Search for file recursively in all subdirectories
     */
    private Path findFileInSubdirectories(String fileName) throws IOException {
        return Files.walk(this.fileStorageLocation)
                .filter(path -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Alternative fixed method if you know the travelRequestId
     */
    public Resource loadFileAsResource(UUID travelRequestId, String fileName) {
        try {
            // Build the correct path: {uploadDir}/{travelRequestId}/{fileName}
            Path requestDirectory = this.fileStorageLocation.resolve(travelRequestId.toString());
            Path filePath = requestDirectory.resolve(fileName).normalize();
            
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName + " in directory " + travelRequestId);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            // FIXED: Find and delete file from any subdirectory
            Path filePath = findFileInSubdirectories(fileName);
            if (filePath != null) {
                Files.deleteIfExists(filePath);
                log.info("File deleted: {}", fileName);
            } else {
                log.warn("File not found for deletion: {}", fileName);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Alternative delete method if you know the travelRequestId
     */
    public void deleteFile(UUID travelRequestId, String fileName) {
        try {
            Path requestDirectory = this.fileStorageLocation.resolve(travelRequestId.toString());
            Path filePath = requestDirectory.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    public boolean fileExists(String fileName) {
        try {
            Path filePath = findFileInSubdirectories(fileName);
            return filePath != null && Files.exists(filePath);
        } catch (Exception ex) {
            return false;
        }
    }

    private String generateFileName(UUID travelRequestId, String documentType, String fileExtension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s_%s%s", 
            travelRequestId, documentType, timestamp, randomId, fileExtension);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return ".bin";
    }
}