package com.example.offreservice.dto.request;

import com.example.offreservice.model.StatutOffre;
import com.example.offreservice.model.TypeOffre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOffreRequest {


    private String titre;

    private String localisation;

    private StatutOffre statutOffre;

    private Long clientId;
    private LocalDate dateDemande;
    private TypeOffre type;
}
