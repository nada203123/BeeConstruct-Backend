package com.dpc.chantierservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommandeRequestDTO {

    private Long chantierId;
    private String nomFournisseur;
    private Double prixHT;
    private Double tva;
    private LocalDate dateCommande;
    private String description;
    private Long situationId;
}