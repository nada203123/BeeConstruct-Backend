package com.dpc.chantierservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "commandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chantierId;

    @Column(nullable = false)
    private String nomFournisseur;

    @Column(nullable = false)
    private Double prixHT;

    @Column(nullable = false)
    private Double tva;

    @Column(nullable = false)
    private Double prixTTC;

    @Column(nullable = false)
    private LocalDate dateCommande;

    @Column(nullable = true)
    private String description;

    @Column(name = "situation_id", nullable = true)
    private Long situationId;
}