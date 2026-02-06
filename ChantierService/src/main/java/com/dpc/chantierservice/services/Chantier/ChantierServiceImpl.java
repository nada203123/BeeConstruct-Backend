package com.dpc.chantierservice.services.Chantier;

import com.dpc.chantierservice.dto.CreateChantierRequest;
import com.dpc.chantierservice.dto.OffreDTO;
import com.dpc.chantierservice.dto.UpdateChantierRequest;
import com.dpc.chantierservice.feign.OffreFeignClient;
import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.Situation;
import com.dpc.chantierservice.model.ChantierStatus;
import com.dpc.chantierservice.repositories.ChantierRepository;
import com.dpc.chantierservice.repositories.SituationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChantierServiceImpl  implements ChantierService {

    private final ChantierRepository chantierRepository;
    private final SituationRepository situationRepository;
    private final OffreFeignClient offreFeignClient;



@Override
    public Chantier createChantier(CreateChantierRequest request) {

    if (request.getOffreId() != null && chantierRepository.existsByOffreId(request.getOffreId())) {
        throw new IllegalStateException("Un chantier existe déjà pour ce offre (ID: " + request.getOffreId() + ")");
    }

        Chantier chantier = Chantier.builder()
                .titre(request.getTitre())
                .localisation(request.getLocalisation())
                .clientId(request.getClientId())
                .client(request.getClient())
                .offreId(request.getOffreId())
                .statut(request.getStatut())
                .coutTotal(request.getCoutTotal())
                .dateDeDebut(request.getDateDeDebut())
                .progression(request.getProgression())
                .cumulPartBeehive(BigDecimal.ZERO)
                .build();


        return chantierRepository.save(chantier);
    }

    @Override
    public Chantier getChantierById(Long id) {
        return chantierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + id));
    }

    @Override
    public List<Chantier> getAllChantiers() {
        return chantierRepository.findAll();
    }


    @Override
    public Chantier updateChantier(Long id, UpdateChantierRequest request) {
        Chantier existingChantier = chantierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + id));

        String token = getCurrentUserToken();
        Long offreId = existingChantier.getOffreId();

        OffreDTO offreDTO = offreFeignClient.getOffreById(offreId, token);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = offreDTO.getDateDemande().format(formatter);

        if (request.getDateDeDebut() != null && !request.getDateDeDebut().isAfter(offreDTO.getDateDemande().atStartOfDay())) {
            throw new IllegalArgumentException("La date de début du chantier doit être postérieure à la date de demande de l'offre (" + formattedDate + ").");
        }

        if (request.getCoutTotal() != null && request.getCoutTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le coût total ne peut pas être négatif.");
        }

        existingChantier.setTitre(request.getTitre());
        existingChantier.setLocalisation(request.getLocalisation());
        existingChantier.setClientId(request.getClientId());
        existingChantier.setClient(request.getClient());
        existingChantier.setCoutTotal(request.getCoutTotal());
        existingChantier.setDateDeDebut(request.getDateDeDebut());


        Chantier chantierMisAJour = chantierRepository.save(existingChantier);

        // Mettre à jour montant restant après la modification du coutTotal
        updateMontantRestant(chantierMisAJour.getId());

        return chantierMisAJour;
    }

    @Override
    public Chantier updateChantierStatus(Long id, ChantierStatus newStatus) {
        Chantier existingChantier = chantierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + id));

        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        existingChantier.setStatut(newStatus);
        return chantierRepository.save(existingChantier);
    }




    @Transactional
    public void updateCumulPartBeehive(Long chantierId) {
        // Verify the chantier exists
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + chantierId));

        // Get all situations for this chantier
        List<Situation> situations = situationRepository.findByChantier(chantier);

        // Calculate the sum of portionBeehiveMontant (filtering out null values)
        BigDecimal totalCumulPartBeehive = situations.stream()
                .map(Situation::getPortionBeehiveMontant)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update the chantier's cumulPartBeehive
        chantier.setCumulPartBeehive(totalCumulPartBeehive);
        chantierRepository.save(chantier);
    }


    @Transactional
    public void updateChiffreAffaireFacture(Long chantierId) {
        // Verify the chantier exists
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + chantierId));

        // Get all situations for this chantier
        List<Situation> situations = situationRepository.findByChantier(chantier);


        BigDecimal montantGlobalTotal = situations.stream()
                .map(Situation::getMontantGlobal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update the chantier's cumulPartBeehive
        chantier.setMontantGlobalTotal(montantGlobalTotal);
        chantierRepository.save(chantier);
    }


    @Transactional
    public void updateMontantRestant(Long chantierId) {
        // Verify the chantier exists
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + chantierId));

        // Get all situations for this chantier
        List<Situation> situations = situationRepository.findByChantier(chantier);


        BigDecimal montantGlobalTotal = situations.stream()
                .map(Situation::getMontantGlobal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantGlobalRestant = chantier.getCoutTotal().subtract(montantGlobalTotal);
                chantier.setMontantRestant(montantGlobalRestant);
        chantierRepository.save(chantier);
    }

    private String getCurrentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return "Bearer " + jwtAuth.getToken().getTokenValue();
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Cannot get authentication token"
        );
    }



    @Override
    @Transactional
    public void deleteChantier(Long id) {
        // Verify the chantier exists
        Chantier chantier = chantierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + id));

        // Get all situations associated with this chantier
        List<Situation> situations = situationRepository.findByChantier(chantier);

        // Delete all associated situations first (cascade delete)
        if (!situations.isEmpty()) {
            situationRepository.deleteAll(situations);
        }

        // Delete the chantier
        chantierRepository.deleteById(id);
    }

    @Override
    public Chantier getChantierByOffreId(Long offreId) {
        return chantierRepository.findByOffreId(offreId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with offre id: " + offreId));
    }


}


