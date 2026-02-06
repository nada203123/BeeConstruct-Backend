package com.dpc.chantierservice.services.Document;

import com.dpc.chantierservice.dto.DocumentFileDownload;
import com.dpc.chantierservice.dto.DocumentRequestDTO;
import com.dpc.chantierservice.model.Document;
import com.dpc.chantierservice.repositories.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Document uploadDocument(DocumentRequestDTO documentRequestDTO, String token) {
        log.info("Uploading document with token: {}", token);
        MultipartFile file = documentRequestDTO.getFile();
        Long chantierId = documentRequestDTO.getChantierId();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required and cannot be empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null ||
                !(originalFileName.toLowerCase().endsWith(".pdf") ||
                        originalFileName.toLowerCase().endsWith(".doc") ||
                        originalFileName.toLowerCase().endsWith(".docx"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF and Word documents (.pdf, .doc, .docx) are allowed"
            );
        }

        try {
            Map<String, String> fileUploadResult = fileStorageService.storeFile(file);
            String storedFileName = fileUploadResult.get("fileName");
            String fileUrl = fileUploadResult.get("fileUrl");

            // Truncate fileName and filePath to 255 characters
            String truncatedFileName = originalFileName.length() > 255 ? originalFileName.substring(0, 255) : originalFileName;
            String truncatedFilePath = fileUrl.length() > 255 ? fileUrl.substring(0, 255) : fileUrl;

            LocalDateTime createdAt = LocalDateTime.now();
            log.info("Setting createdAt to: {}", createdAt);
            boolean isContract = documentRequestDTO.isContract();

            Document document = Document.builder()
                    .chantierId(chantierId)
                    .fileName(truncatedFileName)
                    .filePath(truncatedFilePath)
                    .createdAt(createdAt)
                    .isContract(isContract)
                    .build();

            return documentRepository.save(document);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error storing file: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id: " + id));
    }

    @Override
    public List<Document> getDocumentsByChantierId(Long chantierId) {
        return documentRepository.findByChantierId(chantierId);
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id: " + id));

        try {
            String fileName = document.getFileName();
            if (fileName != null && !fileName.isEmpty()) {
                fileStorageService.deleteFile(fileName);
            }
            documentRepository.delete(document);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error deleting document file: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public DocumentFileDownload downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id: " + id));

        String filePath = document.getFilePath();
        String originalFileName = document.getFileName();

        if (filePath == null || filePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found for document with id: " + id);
        }

        try {
            String objectName = extractObjectNameFromPath(filePath);
            log.info("Extracted object name: {}", objectName);

            InputStream inputStream = fileStorageService.getFile(objectName);
            String contentType = fileStorageService.getContentType(objectName);

            return new DocumentFileDownload(inputStream, contentType, originalFileName);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving file", e);
        }
    }

    private String extractObjectNameFromPath(String filePath) {
        try {
            URI uri = new URI(filePath);
            String path = uri.getPath();
            String basePath = path.split("\\?")[0];
            return basePath.substring(basePath.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid file path: " + filePath, e);
        }
    }
}