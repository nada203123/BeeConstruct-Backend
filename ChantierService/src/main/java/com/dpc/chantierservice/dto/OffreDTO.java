package com.dpc.chantierservice.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class OffreDTO {
    private Long id;
    private String titre;
    private String localisation;
    private Long clientId;
    private String statutOffre;
    private boolean archived;
    private LocalDate dateDemande;
    private String type;
    private String filePath;
    private String fileName;

}
