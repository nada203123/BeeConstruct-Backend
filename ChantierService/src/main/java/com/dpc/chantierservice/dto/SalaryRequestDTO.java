package com.dpc.chantierservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRequestDTO {
    private Long situationId;
    private List<PerDiemSalary> perDiemSalaries;
    private BigDecimal subcontractorAllocation;
    private BigDecimal otherCharges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerDiemSalary {
        private Long employeeId;
        private BigDecimal fixedSalary;
    }
}