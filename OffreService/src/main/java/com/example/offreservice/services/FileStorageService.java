package com.example.offreservice.services;

import com.example.offreservice.config.AppProperties;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Service
public class FileStorageService {
    private final MinioClient minioClient;
    private final String bucketName;
    private final String minioUrl;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    public FileStorageService(AppProperties appProperties) throws Exception {
        this.minioUrl = appProperties.getMinioUrl();
        this.bucketName = appProperties.getMinioBucket();
        this.minioClient = MinioClient.builder()
                .endpoint(appProperties.getMinioUrl())
                .credentials(appProperties.getMinioAccessKey(), appProperties.getMinioSecretKey())
                .build();
        // Ensure the bucket exists
        try {
            if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Created MinIO bucket: {}", bucketName);
            } else {
                logger.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize MinIO bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    public Map<String, String> storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String fileName = generateUniqueFileName(file);
        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("File must be a PDF or Word document");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.equals("application/octet-stream")) {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName != null) {
                if (originalFileName.endsWith(".pdf")) {
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                } else if (originalFileName.endsWith(".doc")) {
                    contentType = "application/msword";
                } else if (originalFileName.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
            }
        }

        try (InputStream fileStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(fileStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build());
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", getPresignedUrl(fileName));
            return response;
        } catch (Exception e) {
            logger.error("Failed to upload the file: {}", e.getMessage(), e);
            throw new IOException("Failed to upload the file: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("Object name cannot be null or empty");
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60 * 60) // 1 hour expiry
                            .build()
            );
        } catch (Exception e) {
            logger.error("Failed to generate presigned URL for object {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Error generating presigned URL", e);
        }
    }

    public void deleteFile(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        try {
            // Extract the file name if it's a full URL
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }

            // No need to sanitize here as we're using an already stored filename
            // The sanitization should happen during upload, not deletion

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            logger.info("Deleted file from MinIO: {}", fileName);
        } catch (Exception e) {
            logger.error("Failed to delete the file {}: {}", fileName, e.getMessage(), e);
            throw new IOException("Failed to delete the file: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null) {
            int lastDot = originalFileName.lastIndexOf(".");
            if (lastDot > 0) {
                extension = originalFileName.substring(lastDot);
            }
        }
        // Sanitize the filename
        String baseName = UUID.randomUUID().toString().replace("-", "");
        return baseName + extension;
    }

    private boolean isValidFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType();

        logger.info("File name: {}", fileName);
        logger.info("Content type: {}", contentType);

        return fileName.endsWith(".pdf") ||
                fileName.endsWith(".doc") ||
                fileName.endsWith(".docx") ||
                (contentType != null && (
                        contentType.equals("application/pdf") ||
                                contentType.equals("application/msword") ||
                                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                ));
    }


    public InputStream getFile(String objectName) throws IOException {
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("Object name cannot be null or empty");
        }

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Failed to download file from MinIO (object: {}): {}", objectName, e.getMessage(), e);
            throw new IOException("Failed to download the file: " + e.getMessage(), e);
        }
    }

    // Add this method to FileStorageService
    public String getContentType(String objectName) throws IOException {
        try {
            io.minio.StatObjectResponse stat = minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            throw new IOException("Failed to get content type for object: " + objectName, e);
        }
    }
}
