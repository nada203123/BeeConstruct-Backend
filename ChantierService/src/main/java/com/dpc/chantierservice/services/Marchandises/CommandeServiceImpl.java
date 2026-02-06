package com.dpc.chantierservice.services.Marchandises;

import com.dpc.chantierservice.dto.CommandeRequestDTO;
import com.dpc.chantierservice.dto.CommandeResponseDTO;
import com.dpc.chantierservice.model.Commande;
import com.dpc.chantierservice.model.Situation;
import com.dpc.chantierservice.repositories.ChantierRepository;
import com.dpc.chantierservice.repositories.CommandeRepository;
import com.dpc.chantierservice.repositories.SituationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ChantierRepository chantierRepository;
    private final SituationRepository situationRepository;

    @Override
    public CommandeResponseDTO addCommande(CommandeRequestDTO commandeRequestDTO) {
        validateCommandeRequest(commandeRequestDTO);
        Commande commande = mapToEntity(commandeRequestDTO);
        calculatePrixTTC(commande);
        Commande savedCommande = commandeRepository.save(commande);
        return mapToResponseDTO(savedCommande);
    }

    @Override
    public CommandeResponseDTO updateCommande(Long id, CommandeRequestDTO commandeRequestDTO) {
        Commande existingCommande = findCommandeById(id);
        validateCommandeRequest(commandeRequestDTO);

        existingCommande.setChantierId(commandeRequestDTO.getChantierId());
        existingCommande.setNomFournisseur(commandeRequestDTO.getNomFournisseur());
        existingCommande.setPrixHT(commandeRequestDTO.getPrixHT());
        existingCommande.setTva(commandeRequestDTO.getTva());
        existingCommande.setDateCommande(commandeRequestDTO.getDateCommande());
        existingCommande.setDescription(commandeRequestDTO.getDescription());
        existingCommande.setSituationId(commandeRequestDTO.getSituationId());

        calculatePrixTTC(existingCommande);
        Commande updatedCommande = commandeRepository.save(existingCommande);
        return mapToResponseDTO(updatedCommande);
    }

    @Override
    public void deleteCommande(Long id) {
        Commande commande = findCommandeById(id);
        commandeRepository.delete(commande);
    }

    @Override
    public CommandeResponseDTO getCommandeById(Long id) {
        Commande commande = findCommandeById(id);
        return mapToResponseDTO(commande);
    }

    @Override
    public List<CommandeResponseDTO> getCommandesByChantierId(Long chantierId) {
        List<Commande> commandes = commandeRepository.findByChantierId(chantierId);
        return commandes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<CommandeResponseDTO> getCommandesByChantierIdAndSituationId(Long chantierId, Long situationId) {
        List<Commande> commandes = commandeRepository.findByChantierIdAndSituationId(chantierId, situationId);
        return commandes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalTTCByChantierId(Long chantierId) {
        List<Commande> commandes = commandeRepository.findByChantierId(chantierId);
        return commandes.stream()
                .mapToDouble(Commande::getPrixTTC)
                .sum();
    }

    @Override
    public Double getTotalTTCByChantierIdAndSituationId(Long chantierId, Long situationId) {
        List<Commande> commandes = commandeRepository.findByChantierIdAndSituationId(chantierId, situationId);
        return commandes.stream()
                .mapToDouble(Commande::getPrixTTC)
                .sum();
    }


    @Override
    public List<String> getDistinctFournisseurs() {
        return commandeRepository.findDistinctFournisseurs();
    }

    private Commande findCommandeById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande not found with id: " + id));
    }

    private void validateCommandeRequest(CommandeRequestDTO commandeRequestDTO) {
        if (commandeRequestDTO.getChantierId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chantier ID is required");
        }
        if (commandeRequestDTO.getNomFournisseur() == null || commandeRequestDTO.getNomFournisseur().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom du fournisseur is required");
        }
        if (commandeRequestDTO.getPrixHT() == null || commandeRequestDTO.getPrixHT() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prix HT must be greater than 0");
        }
        if (commandeRequestDTO.getTva() == null || commandeRequestDTO.getTva() < 5 || commandeRequestDTO.getTva() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TVA must be between 5% and 20%");
        }
        if (commandeRequestDTO.getDateCommande() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date de commande is required");
        }
        // Validate dateCommande against situation's dateSituation (replacing dateDeDebut)
        Situation situation = situationRepository.findById(commandeRequestDTO.getSituationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Situation not found with id: " + commandeRequestDTO.getSituationId()));
        // Convert java.sql.Date to LocalDate safely
        LocalDate situationDate = new java.util.Date(situation.getDateSituation().getTime()).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        LocalDate commandeDate = commandeRequestDTO.getDateCommande();
        if (!commandeDate.getMonth().equals(situationDate.getMonth()) || commandeDate.getYear() != situationDate.getYear()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date de commande must be in the same month as the situation.");
        }
    }

    private Commande mapToEntity(CommandeRequestDTO commandeRequestDTO) {
        return Commande.builder()
                .chantierId(commandeRequestDTO.getChantierId())
                .nomFournisseur(commandeRequestDTO.getNomFournisseur())
                .prixHT(commandeRequestDTO.getPrixHT())
                .tva(commandeRequestDTO.getTva())
                .dateCommande(commandeRequestDTO.getDateCommande())
                .description(commandeRequestDTO.getDescription())
                .situationId(commandeRequestDTO.getSituationId())
                .build();
    }

    private CommandeResponseDTO mapToResponseDTO(Commande commande) {
        CommandeResponseDTO dto = new CommandeResponseDTO();
        dto.setId(commande.getId());
        dto.setChantierId(commande.getChantierId());
        dto.setNomFournisseur(commande.getNomFournisseur());
        dto.setPrixHT(commande.getPrixHT());
        dto.setTva(commande.getTva());
        dto.setPrixTTC(commande.getPrixTTC());
        dto.setDateCommande(commande.getDateCommande());
        dto.setDescription(commande.getDescription());
        dto.setSituationId(commande.getSituationId());
        return dto;
    }

    private void calculatePrixTTC(Commande commande) {
        Double prixTTC = commande.getPrixHT() + (commande.getPrixHT() * commande.getTva() / 100);
        commande.setPrixTTC(prixTTC);
    }
}