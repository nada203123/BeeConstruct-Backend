package com.dpc.chantierservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommandeResponseDTO {

    private Long id;
    private Long chantierId;
    private String nomFournisseur;
    private Double prixHT;
    private Double tva;
    private Double prixTTC;
    private LocalDate dateCommande;
    private String description;
    private Long situationId;
}