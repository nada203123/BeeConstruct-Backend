package com.dpc.chantierservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;
@AllArgsConstructor
@Data
public class DocumentFileDownload {
    private InputStream inputStream; // it's the file content from MinIO.
    private String contentType;
    private String fileName;
}
