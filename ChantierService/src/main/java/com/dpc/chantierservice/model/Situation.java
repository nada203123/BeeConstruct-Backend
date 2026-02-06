package com.dpc.chantierservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "Situation")
@NoArgsConstructor
@AllArgsConstructor
public class Situation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_situation", nullable = false)
    private String nomSituation;

    @Column(name = "date_situation", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateSituation;

    @Column(name = "montant_global", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantGlobal;

    @Column(name = "charge_situation", precision = 10, scale = 2)
    private BigDecimal chargeSituation;

    @Column(name = "montant_net", precision = 10, scale = 2)
    private BigDecimal montantNet;

    @Column(name = "portion_beehive_pourcentage")
    private Integer portionBeehivePourcentage;

    @Column(name = "portion_beehive_montant", precision = 10, scale = 2)
    private BigDecimal portionBeehiveMontant;

    @Column(name = "montant_salaire", precision = 10, scale = 2)
    private BigDecimal montantSalaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Chantier chantier;


    @ElementCollection
    @CollectionTable(name = "situation_employes", joinColumns = @JoinColumn(name = "situation_id"))
    @Column(name = "employe_id")
    private List<Long> employeIds;

    @Column(name = "montant_restant", precision = 10, scale = 2)
    private BigDecimal montantRestant;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Pointage> pointages;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Salaire> salaires;




}
