package com.dpc.chantierservice.dto;

import java.math.BigDecimal;

public class SalaryDTO {

        private Long id;
        private Long situationId;
        private Long employeeId;
        private BigDecimal montantSalaire;
    private BigDecimal avance;
    private BigDecimal loyer;
    private BigDecimal cotisation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSituationId() {
        return situationId;
    }

    public void setSituationId(Long situationId) {
        this.situationId = situationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getMontantSalaire() {
        return montantSalaire;
    }

    public void setMontantSalaire(BigDecimal montantSalaire) {
        this.montantSalaire = montantSalaire;
    }

    public BigDecimal getAvance() {
        return avance;
    }

    public void setAvance(BigDecimal avance) {
        this.avance = avance;
    }

    public BigDecimal getLoyer() {
        return loyer;
    }

    public void setLoyer(BigDecimal loyer) {
        this.loyer = loyer;
    }

    public BigDecimal getCotisation() {
        return cotisation;
    }

    public void setCotisation(BigDecimal cotisation) {
        this.cotisation = cotisation;
    }
}
