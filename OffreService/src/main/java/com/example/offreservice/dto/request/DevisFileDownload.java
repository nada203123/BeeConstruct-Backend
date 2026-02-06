package com.example.offreservice.dto.request;

import java.io.InputStream;

public class DevisFileDownload {
    private final InputStream inputStream;
    private final String contentType;
    private final String fileName;

    public DevisFileDownload(InputStream inputStream, String contentType, String fileName) {
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    // Getters
    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }
}
