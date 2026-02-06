package com.example.offreservice.dto.response;

import com.example.offreservice.model.StatutOffre;
import com.example.offreservice.dto.ClientDTO;
import com.example.offreservice.model.TypeOffre;
import lombok.Data;

import java.time.LocalDate;

@Data

public class OffreDTO {

    private Long id;
    private String titre;
    private String localisation;
    private StatutOffre statutOffre;
    private boolean archived;
    private Long clientId;
    private ClientDTO client;
    private LocalDate dateDemande;
    private TypeOffre type;

}
