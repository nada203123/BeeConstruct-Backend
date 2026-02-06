package com.dpc.chantierservice.dto;

import com.dpc.chantierservice.model.Chantier;
import org.springframework.stereotype.Component;

@Component
public class ChantierMapper {
    public ChantierDTO toDto(Chantier chantier) {
        if (chantier == null) {
            return null;
        }

        return ChantierDTO.builder()
                .id(chantier.getId())
                .titre(chantier.getTitre())
                .localisation(chantier.getLocalisation())
                .clientId(chantier.getClientId())
                .client(chantier.getClient())
                .offreId(chantier.getOffreId())
                .statut(chantier.getStatut())
                .coutTotal(chantier.getCoutTotal())
                .dateDeDebut(chantier.getDateDeDebut())
                .cumulPartBeehive(chantier.getCumulPartBeehive())
                .progression(chantier.getProgression())
                .build();
    }

    public Chantier toEntity(ChantierDTO dto) {
        if (dto == null) {
            return null;
        }

        return Chantier.builder()
                .id(dto.getId())
                .titre(dto.getTitre())
                .localisation(dto.getLocalisation())
                .clientId(dto.getClientId())
                .client(dto.getClient())
                .offreId(dto.getOffreId())
                .statut(dto.getStatut())
                .coutTotal(dto.getCoutTotal())
                .dateDeDebut(dto.getDateDeDebut())
                .cumulPartBeehive(dto.getCumulPartBeehive())
                .progression(dto.getProgression())
                .build();
    }
}
