package com.dpc.chantierservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointageResponseDto {
    private Long id;
    private Long situationId;
    private Long employeId;
    private Map<String, Double> heuresParJour;
    private Double totalHeures;
    private Double nombreJoursTravailles;
}
