package com.example.offreservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Offres")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Offre {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String titre;

        private String localisation;

        @Enumerated(EnumType.STRING)
        private StatutOffre statutOffre;

        @Column(nullable = false)
        private boolean archived = false;

        private Long clientId;

        @Column(nullable = false)
        private LocalDate dateDemande;

        @Enumerated(EnumType.STRING)
        private TypeOffre type;

        @Column(name = "file_path",length = 1024)
        private String filePath;



        @Column(name = "fileName",length = 512)
        private String fileName;



}
