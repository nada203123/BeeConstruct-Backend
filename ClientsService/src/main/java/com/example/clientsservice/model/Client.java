package com.example.clientsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.*;

@Entity
@Table(name = "clients")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_societe", nullable = false)
    private String nomSociete;

    @Column(name = "siege_social", nullable = false)
    private String siegeSocial;

    @Column(name = "adresse_exploitation")
    private String adresse;

    @Column(name = "nom_directeur", nullable = false)
    private String nomDirecteur;

    @Column(name = "prenom_directeur", nullable = false)
    private String prenomDirecteur;

    @Column(name = "telephone_directeur", nullable = false)
    private String telephoneDirecteur;

    @Column(nullable = false)
    private boolean archived = false;

}
