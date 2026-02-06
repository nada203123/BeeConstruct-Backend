package com.dpc.chantierservice.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Data
@Builder
@Entity
@Table(name = "Chantier")

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Chantier {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Getter
    @Column(name = "titre", nullable = false)
    private String titre;

    @Getter
    @Column(name = "localisation", nullable = false)
    private String localisation;

    @Getter
    @Column(name = "montant_global_total", precision = 10, scale = 2)
    private BigDecimal montantGlobalTotal;

    @Getter
    @Column(name = "montant_restant", precision = 10, scale = 2)
    private BigDecimal montantRestant;


    public void setMontantGlobalTotal(BigDecimal montantGlobalTotal) {
        this.montantGlobalTotal = montantGlobalTotal;
    }

    public void setMontantRestant(BigDecimal montantRestant) {
        this.montantRestant = montantRestant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }


    public void setClient(String client) {
        this.client = client;
    }

    public void setStatut(ChantierStatus statut) {
        this.statut = statut;
    }

    public void setCoutTotal(BigDecimal coutTotal) {
        this.coutTotal = coutTotal;
    }

    public void setDateDeDebut(LocalDateTime dateDeDebut) {
        this.dateDeDebut = dateDeDebut;
    }

    @Getter
    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Getter
    @Column(name = "CLIENT", nullable = false)
    private String client;

    @Getter
    @Column(name = "offre_id")
    private Long offreId;


    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ChantierStatus statut;

    @Getter
    @Column(name = "cout_total", precision = 10, scale = 2)
    private BigDecimal coutTotal;

    @Getter
    @Column(name = "date_debut")
    private LocalDateTime dateDeDebut;

    @Getter
    @Column(name = "cumul_part_beehive", precision = 10, scale = 2)
    private BigDecimal cumulPartBeehive;

    @Getter
    @Column(name = "progression", precision = 5, scale = 2)
    private Integer progression;


    public void setOffreId(Long offreId) {
        this.offreId = offreId;
    }

    public void setCumulPartBeehive(BigDecimal cumulPartBeehive) {
        this.cumulPartBeehive = cumulPartBeehive;
    }

    public void setProgression(Integer progression) {
        this.progression = progression;
    }





}
