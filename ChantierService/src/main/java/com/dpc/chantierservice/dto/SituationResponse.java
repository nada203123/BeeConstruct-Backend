package com.dpc.chantierservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SituationResponse {
    private Long id;

    private String nomSituation;
    private Date dateSituation;
    private BigDecimal montantGlobal;
    private BigDecimal montantNet;
    private BigDecimal portionBeehiveMontant;
    private ChantierDTO chantier;
    private List<EmployeDTO> employes;
    private BigDecimal montantSalaire;
    private BigDecimal chargeSituation;
    private Integer portionBeehivePourcentage;
    private BigDecimal montantRestant;
}
