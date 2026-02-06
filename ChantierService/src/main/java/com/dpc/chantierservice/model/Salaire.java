package com.dpc.chantierservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
@Entity
@Table(name = "salaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "situation_id", nullable = false)
    private Long situationId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "montantSalaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantSalaire;


    @Column(name = "avance", nullable = true, precision = 10, scale = 2)
    private BigDecimal avance;

    @Column(name = "loyer", nullable = true, precision = 10, scale = 2)
    private BigDecimal loyer;

    @Column(name = "cotisation", nullable = true, precision = 10, scale = 2)
    private BigDecimal cotisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Situation situation;


}
