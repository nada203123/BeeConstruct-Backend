package com.dpc.chantierservice.services.Marchandises;

import com.dpc.chantierservice.dto.CommandeRequestDTO;
import com.dpc.chantierservice.dto.CommandeResponseDTO;

import java.util.List;

public interface CommandeService {

    CommandeResponseDTO addCommande(CommandeRequestDTO commandeRequestDTO);

    CommandeResponseDTO updateCommande(Long id, CommandeRequestDTO commandeRequestDTO);

    void deleteCommande(Long id);

    CommandeResponseDTO getCommandeById(Long id);


    List<CommandeResponseDTO> getCommandesByChantierId(Long chantierId);

    List<CommandeResponseDTO> getCommandesByChantierIdAndSituationId(Long chantierId, Long situationId);

    Double getTotalTTCByChantierId(Long chantierId);

    Double getTotalTTCByChantierIdAndSituationId(Long chantierId, Long situationId);

    List<String> getDistinctFournisseurs();
}