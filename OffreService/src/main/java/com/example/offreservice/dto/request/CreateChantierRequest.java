package com.example.offreservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateChantierRequest {

    private String titre;
    private String localisation;
    private Long clientId;
    private String client;
    private String statut;
    private Long offreId;
    private Integer progression;

}
