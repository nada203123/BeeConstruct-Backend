package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.DocumentFileDownload;
import com.dpc.chantierservice.dto.DocumentRequestDTO;
import com.dpc.chantierservice.model.Document;
import com.dpc.chantierservice.services.Document.DocumentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("chantierId") Long chantierId,
            @RequestParam("file") @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) MultipartFile file,
            @RequestParam(value = "isContract", defaultValue = "false") boolean isContract,
            @RequestHeader("Authorization") String token) {
        DocumentRequestDTO documentRequestDTO = new DocumentRequestDTO();
        documentRequestDTO.setChantierId(chantierId);
        documentRequestDTO.setFile(file);
        documentRequestDTO.setContract(isContract); // Set isContract in the DTO
        Document createdDocument = documentService.uploadDocument(documentRequestDTO, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/chantier/{chantierId}")
    public ResponseEntity<List<Document>> getDocumentsByChantierId(@PathVariable Long chantierId) {
        List<Document> documents = documentService.getDocumentsByChantierId(chantierId);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id) {
        DocumentFileDownload download = documentService.downloadDocument(id);
        InputStreamResource resource = new InputStreamResource(download.getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"")
                .body(resource);
    }
}