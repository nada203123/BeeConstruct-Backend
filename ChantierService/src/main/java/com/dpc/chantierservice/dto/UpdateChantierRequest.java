package com.dpc.chantierservice.dto;

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
public class UpdateChantierRequest {
    private String titre;
    private String localisation;
    private Long clientId;
    private String client;
    private BigDecimal coutTotal;
    private LocalDateTime dateDeDebut;
}
