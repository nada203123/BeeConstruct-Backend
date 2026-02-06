package com.example.offreservice.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class HistoryRequestDTO {

    private Long offreId;
    private String comment;

    private MultipartFile file;
}
