package com.dpc.chantierservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SituationRequest {

    private String nomSituation;
    private Date dateSituation;
    private BigDecimal montantGlobal;
    private BigDecimal chargeSituation;
    private Integer portionBeehivePourcentage;
    private Long chantierId;
    private List<Long> employeIds;
}
