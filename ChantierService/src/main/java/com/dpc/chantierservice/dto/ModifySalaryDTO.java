package com.dpc.chantierservice.dto;

import java.math.BigDecimal;

public class ModifySalaryDTO {
    public BigDecimal getMontantSalaire() {
        return montantSalaire;
    }

    public void setMontantSalaire(BigDecimal montantSalaire) {
        this.montantSalaire = montantSalaire;
    }

    private BigDecimal montantSalaire;

}
