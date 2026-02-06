package com.dpc.chantierservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreatePointageDto {

    @NotNull(message = "L'ID de la situation est obligatoire")
    private Long situationId;

    @NotNull(message = "L'ID de l'employé est obligatoire")
    private Long employeId;

    private Map<String, Double> heuresParJour;
}
