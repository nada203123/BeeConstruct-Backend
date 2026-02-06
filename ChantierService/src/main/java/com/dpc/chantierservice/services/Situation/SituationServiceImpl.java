package com.dpc.chantierservice.services.Situation;

import com.dpc.chantierservice.dto.*;
import com.dpc.chantierservice.feign.EmployeFeignClient;
import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.Salaire;
import com.dpc.chantierservice.model.Situation;
import com.dpc.chantierservice.repositories.ChantierRepository;
import com.dpc.chantierservice.repositories.SituationRepository;
import com.dpc.chantierservice.services.Chantier.ChantierServiceImpl;
import com.dpc.chantierservice.services.Pointage.PointageServiceImpl;
import com.dpc.chantierservice.services.Salaire.SalaireServiceImpl;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.context.annotation.Lazy;

@Service
@RequiredArgsConstructor
@Slf4j
public class SituationServiceImpl implements SituationService {
    private final SituationRepository situationRepository;
    private final ChantierRepository chantierRepository;
    private final EmployeFeignClient employeFeignClient;
    private final ChantierMapper chantierMapper;
    private final ChantierServiceImpl chantierServiceImpl;
    private  PointageServiceImpl pointageServiceImpl;
    private final SalaireServiceImpl salaireServiceImpl;



    @Autowired
    @Lazy
    public void setPointageServiceImpl(PointageServiceImpl pointageServiceImpl) {
        this.pointageServiceImpl = pointageServiceImpl;
    }

    @Transactional
    @Override
    public SituationResponse createSituation(SituationRequest request) {
        String token = getCurrentUserToken();
        // Find the chantier
        Chantier chantier = chantierRepository.findById(request.getChantierId())
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + request.getChantierId()));

        // Validation 1: chargeSituation doit être inférieure à montantGlobal du situation
        if (request.getChargeSituation().compareTo(request.getMontantGlobal()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La charge de la situation (" + request.getChargeSituation() +
                            ") doit être inférieure au montant global (" + request.getMontantGlobal() + ")");
        }

        Date chantierStartDate = Date.from(chantier.getDateDeDebut().atZone(ZoneId.systemDefault()).toInstant());

        if (request.getDateSituation().before(chantierStartDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de la situation (" + sdf.format(request.getDateSituation()) +
                            ") doit être postérieure à la date de début du chantier (" + sdf.format(chantierStartDate) + ")");
        }

        if (chantier.getCoutTotal() != null && request.getMontantGlobal().compareTo(chantier.getCoutTotal()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le montant global de la situation (" + request.getMontantGlobal() +
                            ") ne peut pas dépasser le coût total du chantier (" + chantier.getCoutTotal() + ")");
        }

        BigDecimal sommeMontantsExistants = situationRepository.sumMontantGlobalByChantierId(chantier.getId());
        if (sommeMontantsExistants == null) {
            sommeMontantsExistants = BigDecimal.ZERO;
        }

        if (chantier.getCoutTotal() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le coût total du chantier n'est pas défini. Veuillez définir le coût total avant d'ajouter une situation.");
        }

        BigDecimal currentMontantRestant = chantier.getCoutTotal().subtract(sommeMontantsExistants);

        // NEW VALIDATION: Check if new situation amount exceeds remaining amount
        if (request.getMontantGlobal().compareTo(currentMontantRestant) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le montant global de la situation (" + request.getMontantGlobal() +
                            ") ne peut pas dépasser le montant restant (" + currentMontantRestant + ")");
        }

        BigDecimal sommeTotale = sommeMontantsExistants.add(request.getMontantGlobal());

        // Vérifier que la somme totale ne dépasse pas le coutTotal du chantier
        if (chantier.getCoutTotal() != null && sommeTotale.compareTo(chantier.getCoutTotal()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La somme des montants globaux des situations (" + sommeTotale +
                            ") ne peut pas dépasser le coût total du chantier (" + chantier.getCoutTotal() + ")");
        }

        // NEW VALIDATION: Check for overlapping monthly periods

        List<Situation> existingSituations = situationRepository.findByChantierIdOrderByDateSituation(chantier.getId());
        Date newSituationDate = request.getDateSituation();
        Calendar cal = Calendar.getInstance();
        cal.setTime(newSituationDate);
        cal.add(Calendar.MONTH, 1);
        Date newSituationEndDate = cal.getTime();

        for (Situation existingSituation : existingSituations) {
            Date existingDate = existingSituation.getDateSituation();
            cal.setTime(existingDate);
            cal.add(Calendar.MONTH, 1);
            Date existingEndDate = cal.getTime();

            // Vérifie si les périodes se chevauchent
            boolean overlaps =
                    (newSituationDate.after(existingDate) && newSituationDate.before(existingEndDate)) ||  // Nouvelle situation commence pendant une période existante
                            (newSituationEndDate.after(existingDate) && newSituationEndDate.before(existingEndDate)) ||  // Nouvelle situation se termine pendant une période existante
                            (newSituationDate.before(existingDate) && newSituationEndDate.after(existingEndDate)) ||  // Nouvelle situation englobe complètement une période existante
                            newSituationDate.equals(existingDate);
            if (overlaps) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La période de la nouvelle situation (" + sdf.format(newSituationDate) + " - " + sdf.format(newSituationEndDate) +
                                ") chevauche avec une situation existante (" + sdf.format(existingDate) + " - " + sdf.format(existingEndDate) + ")");
            }
        }


        // Calculate the derived values
        BigDecimal montantNet = request.getMontantGlobal().subtract(request.getChargeSituation());

        // Calculate the portion beehive amount based on the percentage
        BigDecimal percentage = BigDecimal.valueOf(request.getPortionBeehivePourcentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal montantPortionBeehive = montantNet.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

        // Calculate salary amount
        BigDecimal montantSalaire = montantNet.subtract(montantPortionBeehive);
        BigDecimal montantRestant = chantier.getCoutTotal().subtract(sommeTotale);
        // Create the situation entity
        Situation situation = Situation.builder()
                .nomSituation(request.getNomSituation())
                .dateSituation(request.getDateSituation())
                .montantGlobal(request.getMontantGlobal())
                .chargeSituation(request.getChargeSituation())
                .montantNet(montantNet)
                .portionBeehivePourcentage(request.getPortionBeehivePourcentage())
                .portionBeehiveMontant(montantPortionBeehive)
                .montantSalaire(montantSalaire)
                .montantRestant(montantRestant)
                .chantier(chantier)
                .employeIds(request.getEmployeIds())
                .build();

        situation = situationRepository.save(situation);

        if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
            for (Long employeId : request.getEmployeIds()) {
                try {
                    Salaire salaire = Salaire.builder()
                            .situationId(situation.getId())
                            .employeeId(employeId)
                            .montantSalaire(BigDecimal.ZERO) // or a salary amount based on your business logic
                            .avance(BigDecimal.ZERO) // initialize as needed
                            .loyer(BigDecimal.ZERO)  // initialize as needed
                            .cotisation(BigDecimal.ZERO) // initialize as needed
                            .build();

                    System.out.println("Adding salary for employeeId: " + employeId);
                    salaireServiceImpl.addSalary(salaire);
                    System.out.println("Salary added successfully for employeeId: " + employeId);

                    CreatePointageDto createPointageDto = CreatePointageDto.builder()
                            .situationId(situation.getId())
                            .employeId(employeId)
                            .heuresParJour(new HashMap<>()) // Initialize with empty map - to be filled later
                            .build();

                    pointageServiceImpl.createPointage(createPointageDto);


                } catch (RuntimeException e) {

                    System.err.println("Erreur lors de la création du pointage pour l'employé " + employeId + ": " + e.getMessage());

                }
            }
        }
        chantierServiceImpl.updateCumulPartBeehive(situation.getChantier().getId());
        chantierServiceImpl.updateChiffreAffaireFacture(situation.getChantier().getId());
        chantierServiceImpl.updateMontantRestant(situation.getChantier().getId());

        List<EmployeDTO> employes = employeFeignClient.getEmployesByIds(request.getEmployeIds(),token);


        ChantierDTO chantierDTO = chantierMapper.toDto(chantier);

        // Build response
        return SituationResponse.builder()
                .id(situation.getId())
                .nomSituation(situation.getNomSituation())
                .dateSituation(situation.getDateSituation())
                .montantGlobal(situation.getMontantGlobal())
                .chargeSituation(situation.getChargeSituation())
                .montantNet(situation.getMontantNet())
                .portionBeehivePourcentage(situation.getPortionBeehivePourcentage())
                .portionBeehiveMontant(situation.getPortionBeehiveMontant())
                .montantSalaire(situation.getMontantSalaire())
                .montantRestant(situation.getMontantRestant())
                .chantier(chantierDTO)
                .employes(employes)
                .build();
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
    public SituationResponse getSituationById(Long id) {
        Situation situation = situationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Situation non trouvée avec l'id: " + id));

        return mapToSituationResponse(situation);
    }

    @Override
    public List<SituationResponse> getAllSituations() {
        List<Situation> situations = situationRepository.findAll();
        return situations.stream()
                .map(this::mapToSituationResponse)
                .toList();
    }

    @Override
    public List<SituationResponse> getSituationsByChantierId(Long chantierId) {
        // Vérifier que le chantier existe
        if (!chantierRepository.existsById(chantierId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Chantier non trouvé avec l'id: " + chantierId);
        }

        List<Situation> situations = situationRepository.findByChantierId(chantierId);
        return situations.stream()
                .map(this::mapToSituationResponse)
                .toList();
    }


    @Override
    public void deleteSituation(Long id) {
        // Find the situation
        Situation situation = situationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Situation non trouvée avec l'id: " + id));

          Long chantierId = situation.getChantier().getId();




        // Delete the situation
        situationRepository.delete(situation);
        chantierServiceImpl.updateCumulPartBeehive(chantierId);
        chantierServiceImpl.updateChiffreAffaireFacture(chantierId);
        chantierServiceImpl.updateMontantRestant(chantierId);


    }

    // Méthode helper pour mapper Situation vers SituationResponse


    private SituationResponse mapToSituationResponse(Situation situation) {
        String token = getCurrentUserToken();
        List<EmployeDTO> employes = Collections.emptyList();

        try {
            employes = employeFeignClient.getEmployesByIds(situation.getEmployeIds(), token);
        } catch (FeignException.NotFound e) {
            // If employees are not found, filter out the missing IDs
            List<Long> validEmployeIds = situation.getEmployeIds().stream()
                    .filter(id -> {
                        try {
                            employeFeignClient.getEmployeById(id, token);
                            return true;
                        } catch (Exception ex) {
                            return false;
                        }
                    }).collect(Collectors.toList());

            // Update the situation if some employees were missing
            if (validEmployeIds.size() != situation.getEmployeIds().size()) {
                situation.setEmployeIds(validEmployeIds);
                situationRepository.save(situation);
                employes = employeFeignClient.getEmployesByIds(validEmployeIds, token);
            }
        }

        ChantierDTO chantierDTO = chantierMapper.toDto(situation.getChantier());

        return SituationResponse.builder()
                .id(situation.getId())
                .nomSituation(situation.getNomSituation())
                .dateSituation(situation.getDateSituation())
                .montantGlobal(situation.getMontantGlobal())
                .chargeSituation(situation.getChargeSituation())
                .montantNet(situation.getMontantNet())
                .portionBeehivePourcentage(situation.getPortionBeehivePourcentage())
                .portionBeehiveMontant(situation.getPortionBeehiveMontant())
                .montantSalaire(situation.getMontantSalaire())
                .montantRestant(situation.getMontantRestant())
                .chantier(chantierDTO)
                .employes(employes)
                .build();
    }

    @Transactional
    @Override
    public SituationResponse updateSituation(Long situationId, SituationRequest request) {
        String token = getCurrentUserToken();

        // Find the existing situation
        Situation existingSituation = situationRepository.findById(situationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Situation not found with id: " + situationId));

        // Find the chantier
        Chantier chantier = chantierRepository.findById(request.getChantierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Chantier not found with id: " + request.getChantierId()));

        // Validation 1: chargeSituation doit être inférieure à montantGlobal du situation
        if (request.getChargeSituation().compareTo(request.getMontantGlobal()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La charge de la situation (" + request.getChargeSituation() +
                            ") doit être inférieure au montant global (" + request.getMontantGlobal() + ")");
        }

        if (chantier.getCoutTotal() != null && request.getMontantGlobal().compareTo(chantier.getCoutTotal()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le montant global de la situation (" + request.getMontantGlobal() +
                            ") ne peut pas dépasser le coût total du chantier (" + chantier.getCoutTotal() + ")");
        }

        // Calculate sum of all situations except the one being updated
        BigDecimal sommeMontantsExistants = situationRepository.sumMontantGlobalByChantierIdExcludingSituation(
                chantier.getId(), situationId);
        if (sommeMontantsExistants == null) {
            sommeMontantsExistants = BigDecimal.ZERO;
        }

        BigDecimal currentMontantRestant = chantier.getCoutTotal().subtract(sommeMontantsExistants);

        // NEW VALIDATION: Check if updated situation amount exceeds remaining amount
        if (request.getMontantGlobal().compareTo(currentMontantRestant) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le montant global de la situation (" + request.getMontantGlobal() +
                            ") ne peut pas dépasser le montant restant (" + currentMontantRestant + ")");
        }

        BigDecimal sommeTotale = sommeMontantsExistants.add(request.getMontantGlobal());

        // Vérifier que la somme totale ne dépasse pas le coutTotal du chantier
        if (chantier.getCoutTotal() != null && sommeTotale.compareTo(chantier.getCoutTotal()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La somme des montants globaux des situations (" + sommeTotale +
                            ") ne peut pas dépasser le coût total du chantier (" + chantier.getCoutTotal() + ")");
        }

        // NEW VALIDATION: Check for overlapping monthly periods (excluding current situation)
        List<Situation> existingSituations = situationRepository.findByChantierIdAndIdNotOrderByDateSituation(
                chantier.getId(), situationId);
        Date newSituationDate = request.getDateSituation();
        Calendar cal = Calendar.getInstance();
        cal.setTime(newSituationDate);
        cal.add(Calendar.MONTH, 1);
        Date newSituationEndDate = cal.getTime();

        for (Situation otherSituation : existingSituations) {
            Date existingDate = otherSituation.getDateSituation();
            cal.setTime(existingDate);
            cal.add(Calendar.MONTH, 1);
            Date existingEndDate = cal.getTime();

            // Vérifie si les périodes se chevauchent
            boolean overlaps =
                    (newSituationDate.after(existingDate) && newSituationDate.before(existingEndDate)) ||  // Nouvelle situation commence pendant une période existante
                            (newSituationEndDate.after(existingDate) && newSituationEndDate.before(existingEndDate)) ||  // Nouvelle situation se termine pendant une période existante
                            (newSituationDate.before(existingDate) && newSituationEndDate.after(existingEndDate)) ||  // Nouvelle situation englobe complètement une période existante
                            newSituationDate.equals(existingDate);
            if (overlaps) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La période de la nouvelle situation (" + sdf.format(newSituationDate) + " - " + sdf.format(newSituationEndDate) +
                                ") chevauche avec une situation existante (" + sdf.format(existingDate) + " - " + sdf.format(existingEndDate) + ")");
            }
        }

        // Calculate the derived values
        BigDecimal montantNet = request.getMontantGlobal().subtract(request.getChargeSituation());

        // Calculate the portion beehive amount based on the percentage
        BigDecimal percentage = BigDecimal.valueOf(request.getPortionBeehivePourcentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal montantPortionBeehive = montantNet.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

        // Calculate salary amount
        BigDecimal montantSalaire = montantNet.subtract(montantPortionBeehive);
        BigDecimal montantRestant = chantier.getCoutTotal().subtract(sommeTotale);



        if (request.getEmployeIds() != null) {
            Set<Long> currentEmployeeIds = Optional.ofNullable(existingSituation.getEmployeIds())
                    .map(HashSet::new)
                    .orElseGet(HashSet::new);

            Set<Long> newEmployeeIds = new HashSet<>(request.getEmployeIds());

            // Identify removed employees
            Set<Long> removedEmployees = new HashSet<>(currentEmployeeIds);
            removedEmployees.removeAll(newEmployeeIds);

            // Delete pointage for removed employees
            for (Long employeId : removedEmployees) {
                try {
                    pointageServiceImpl.deletePointageBySituationIdAndEmployeId(situationId, employeId);
                } catch (RuntimeException e) {
                    log.error("Error deleting pointage for employee {} in situation {}: {}", employeId, situationId, e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to delete pointage for employee " + employeId);
                }
                try {
                    salaireServiceImpl.deleteBySituationIdAndEmployeeId(situationId, employeId);
                } catch (RuntimeException e) {
                    log.error("Error deleting salary for employee {} in situation {}: {}", employeId, situationId, e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to delete salary for employee " + employeId);
                }
            }


            Set<Long> addedEmployees = new HashSet<>(newEmployeeIds);
            addedEmployees.removeAll(currentEmployeeIds);

            // Create pointage only for newly added employees
            if (!addedEmployees.isEmpty()) {
                // Convert java.sql.Date to LocalDate safely
                LocalDate situationDate;
                if (existingSituation.getDateSituation() instanceof java.sql.Date) {
                    situationDate = ((java.sql.Date) existingSituation.getDateSituation()).toLocalDate();
                } else {
                    situationDate = existingSituation.getDateSituation().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                Map<String, Double> defaultHours = new LinkedHashMap<>();
                LocalDate startDate = situationDate;
                LocalDate endDate = situationDate.plusDays(30); // Inclusive of start date, so 30 days after

                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    String formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    defaultHours.put(formattedDate, 0.0);
                }

                for (Long employeId : addedEmployees) {
                    try {
                        CreatePointageDto createPointageDto = CreatePointageDto.builder()
                                .situationId(existingSituation.getId())
                                .employeId(employeId)
                                .heuresParJour(defaultHours)
                                .build();

                        pointageServiceImpl.createPointage(createPointageDto);
                    } catch (Exception e) {
                        log.error("Error creating pointage for employee {}: {}", employeId, e.getMessage(), e);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to create pointage for employee " + employeId);
                    }
                    try {
                        Salaire salaire = Salaire.builder()
                                .situationId(existingSituation.getId())
                                .employeeId(employeId)
                                .montantSalaire(BigDecimal.ZERO) // Or calculate appropriately
                                .avance(BigDecimal.ZERO)
                                .loyer(BigDecimal.ZERO)
                                .cotisation(BigDecimal.ZERO)
                                .build();

                        salaireServiceImpl.addSalary(salaire);
                    } catch (Exception e) {
                        log.error("Error creating salary for employee {}: {}", employeId, e.getMessage(), e);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to create salary for employee " + employeId);
                    }
                }
            }
        }

        existingSituation.setNomSituation(request.getNomSituation());
        existingSituation.setDateSituation(request.getDateSituation());
        existingSituation.setMontantGlobal(request.getMontantGlobal());
        existingSituation.setChargeSituation(request.getChargeSituation());
        existingSituation.setMontantNet(montantNet);
        existingSituation.setPortionBeehivePourcentage(request.getPortionBeehivePourcentage());
        existingSituation.setPortionBeehiveMontant(montantPortionBeehive);
        existingSituation.setMontantSalaire(montantSalaire);
        existingSituation.setMontantRestant(montantRestant);
        existingSituation.setChantier(chantier);
        existingSituation.setEmployeIds(request.getEmployeIds());

        Situation updatedSituation = situationRepository.save(existingSituation);
        chantierServiceImpl.updateCumulPartBeehive(updatedSituation.getChantier().getId());
        chantierServiceImpl.updateChiffreAffaireFacture(updatedSituation.getChantier().getId());
        chantierServiceImpl.updateMontantRestant(updatedSituation.getChantier().getId());

        List<EmployeDTO> employes = employeFeignClient.getEmployesByIds(request.getEmployeIds(), token);

        ChantierDTO chantierDTO = chantierMapper.toDto(chantier);

        // Build response
        return SituationResponse.builder()
                .id(updatedSituation.getId())
                .nomSituation(updatedSituation.getNomSituation())
                .dateSituation(updatedSituation.getDateSituation())
                .montantGlobal(updatedSituation.getMontantGlobal())
                .chargeSituation(updatedSituation.getChargeSituation())
                .montantNet(updatedSituation.getMontantNet())
                .portionBeehivePourcentage(updatedSituation.getPortionBeehivePourcentage())
                .portionBeehiveMontant(updatedSituation.getPortionBeehiveMontant())
                .montantSalaire(updatedSituation.getMontantSalaire())
                .montantRestant(updatedSituation.getMontantRestant())
                .chantier(chantierDTO)
                .employes(employes)
                .build();
    }
    }

