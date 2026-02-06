package com.example.offreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChantierDTO {
    private Long id;
    private String titre;
    private String localisation;
    private Long clientId;
    private String clientFirstName;
    private String clientLastName;
    private String statut;
    private Double coutTotal;
    private Date dateDeDebut;
}
