package com.dpc.chantierservice.dto;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class DocumentRequestDTO {
    private Long chantierId;
    private MultipartFile file;
    private boolean isContract = false;

}
