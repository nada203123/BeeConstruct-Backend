package com.dpc.chantierservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryResponseDTO {
    private Long employeeId;
    private String employeeName;
    private String employeeType;
    private BigDecimal salaryAmount;
    private YearMonth month;
    private String calculationDetails;

    private BigDecimal advance;
    private BigDecimal rent;
    private BigDecimal contribution;
}