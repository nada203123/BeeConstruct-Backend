package com.dpc.chantierservice.services.Document;

import com.dpc.chantierservice.dto.DocumentFileDownload;
import com.dpc.chantierservice.dto.DocumentRequestDTO;
import com.dpc.chantierservice.model.Document;
import java.util.List;
public interface DocumentService {
    Document uploadDocument(DocumentRequestDTO documentRequestDTO, String token);
    Document getDocumentById(Long id);
    List<Document> getDocumentsByChantierId(Long chantierId);
    void deleteDocument(Long id);
    DocumentFileDownload downloadDocument(Long id);
}
