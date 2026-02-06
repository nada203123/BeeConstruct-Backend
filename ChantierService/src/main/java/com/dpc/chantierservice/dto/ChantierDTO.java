package com.dpc.chantierservice.dto;

import com.dpc.chantierservice.model.ChantierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChantierDTO {
    private Long id;
    private String titre;
    private String localisation;
    private Long clientId;
    private String client;
    private Long offreId;
    private ChantierStatus statut;
    private BigDecimal coutTotal;
    private LocalDateTime dateDeDebut;
    private BigDecimal cumulPartBeehive;
    private Integer progression;
}
