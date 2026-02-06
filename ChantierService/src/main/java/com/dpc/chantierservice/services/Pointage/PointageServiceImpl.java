package com.dpc.chantierservice.services.Pointage;

import com.dpc.chantierservice.dto.CreatePointageDto;
import com.dpc.chantierservice.dto.ModifySalaryDTO;
import com.dpc.chantierservice.dto.PointageResponseDto;
import com.dpc.chantierservice.model.Pointage;
import com.dpc.chantierservice.model.Situation;
import com.dpc.chantierservice.repositories.PointageRepository;
import com.dpc.chantierservice.repositories.SituationRepository;
import com.dpc.chantierservice.services.Salaire.SalaireService;
import com.dpc.chantierservice.services.Situation.SituationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointageServiceImpl implements PointageService {

    private final PointageRepository pointageRepository;
    private final SituationRepository situationRepository;
    private final SalaireService salaireService;
    private final @Lazy SituationService situationService;

    @Override
    public PointageResponseDto createPointage(CreatePointageDto createPointageDto) {
        // Vérifier que la situation existe
        Situation situation = situationRepository.findById(createPointageDto.getSituationId())
                .orElseThrow(() -> new RuntimeException("Situation non trouvée avec l'ID: " + createPointageDto.getSituationId()));


        pointageRepository.findByEmployeIdAndSituationId(createPointageDto.getEmployeId(), createPointageDto.getSituationId())
                .ifPresent(p -> {
                    throw new RuntimeException("Un pointage existe déjà pour l'employé avec l'ID: " + createPointageDto.getEmployeId() +
                            " et la situation avec l'ID: " + createPointageDto.getSituationId());
                });

        validateHeuresParJour(createPointageDto.getHeuresParJour());

        // Créer et sauvegarder l'entité Pointage
        Pointage pointage = Pointage.builder()
                .situation(situation)
                .employeId(createPointageDto.getEmployeId())
                .heuresParJour(createPointageDto.getHeuresParJour())
                .build();

        Pointage savedPointage = pointageRepository.save(pointage);

        // Convertir l'entité sauvegardée en DTO de réponse
        return convertToDto(savedPointage);
    }

    private void validateHeuresParJour(Map<String, Double> heuresParJour) {
        if (heuresParJour == null || heuresParJour.isEmpty()) {
            throw new RuntimeException("Les heures par jour ne peuvent pas être vides");
        }

        for (Map.Entry<String, Double> entry : heuresParJour.entrySet()) {
            String date = entry.getKey();
            Double heures = entry.getValue();

            // Valider le format de la date (simple validation)
            if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                throw new RuntimeException("Format de date invalide: " + date + ". Utilisez YYYY-MM-DD");
            }

            // Valider que les heures sont positives et raisonnables
            if (heures == null || heures < 0 || heures > 24) {
                throw new RuntimeException("Nombre d'heures invalide pour le " + date + ": " + heures);
            }
        }
    }


    @Override
    public List<PointageResponseDto> getAllPointages() {
        List<Pointage> pointages = pointageRepository.findAll();

        return pointages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PointageResponseDto convertToDto(Pointage pointage) {
        return PointageResponseDto.builder()
                .id(pointage.getId())
                .situationId(pointage.getSituation().getId())
                .employeId(pointage.getEmployeId())
                .heuresParJour(pointage.getHeuresParJour())
                .totalHeures(calculateTotalHeures(pointage.getHeuresParJour()))
                .nombreJoursTravailles(calculateNombreJoursTravailles(pointage.getHeuresParJour()))
                .build();
    }







    private Double calculateTotalHeures(Map<String, Double> heuresParJour) {
        if (heuresParJour == null || heuresParJour.isEmpty()) {
            return 0.0;
        }
        return heuresParJour.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private Double calculateNombreJoursTravailles(Map<String, Double> heuresParJour) {
        if (heuresParJour == null || heuresParJour.isEmpty()) {
            return 0.0;
        }
        // Calculer le nombre de jours basé sur le total d'heures divisé par 8
        Double totalHeures = calculateTotalHeures(heuresParJour);
        return (Double) (totalHeures / 8.0);
    }

    @Override
    public PointageResponseDto getPointageById(Long id) {
        Pointage pointage = pointageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pointage not found with ID: " + id));

        return convertToDto(pointage);
    }
    @Override
    public PointageResponseDto getPointageBySituationAndEmploye(Long situationId, Long employeId) {
        Pointage pointage = pointageRepository.findBySituationIdAndEmployeId(situationId, employeId)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Pointage not found for situation ID %d and employee ID %d", situationId, employeId)
                ));

        return convertToDto(pointage);
    }

    @Override
    public List<PointageResponseDto> getPointagesBySituationId(Long situationId) {
        List<Pointage> pointages = pointageRepository.findBySituationId(situationId);
        return pointages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double getSumNombreJoursTravaillesBySituationId(Long situationId) {
        List<Pointage> pointages = pointageRepository.findBySituationId(situationId);
        System.out.println(pointages);
        return pointages.stream()
                .map(this::convertToDto)
                .mapToDouble(PointageResponseDto::getNombreJoursTravailles)
                .sum();
    }





    @Override
    public PointageResponseDto updateHeuresJour(Long pointageId, String date, Double heures) {
        Pointage pointage = pointageRepository.findById(pointageId)
                .orElseThrow(() -> new RuntimeException("Pointage not found with ID: " + pointageId));

        // Valider la date et les heures
        if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            throw new RuntimeException("Format de date invalide: " + date);
        }

    // Validate that the date exists in heuresParJour
    if (!pointage.getHeuresParJour().containsKey(date)) {
        throw new RuntimeException("La date " + date + " n'existe pas dans les jours enregistrés pour ce pointage");
    }

    if (heures < 0 || heures > 24) {
            throw new RuntimeException("Nombre d'heures invalide: " + heures);
        }

        // Mettre à jour les heures pour ce jour
        pointage.getHeuresParJour().put(date, heures);

        // Si heures = 0, on peut choisir de supprimer l'entrée
        if (heures == 0) {
            pointage.getHeuresParJour().remove(date);
        }

        Pointage savedPointage = pointageRepository.save(pointage);

        List<Pointage> pointages = pointageRepository.findBySituationId(savedPointage.getSituation().getId());

        double totalNbJours = getSumNombreJoursTravaillesBySituationId(savedPointage.getSituation().getId());
        BigDecimal montantSalaireTotal = situationService.getSituationById(savedPointage.getSituation().getId()).getMontantSalaire();

        BigDecimal montantSalaireParJour = BigDecimal.ZERO;
        if (totalNbJours > 0) {
            montantSalaireParJour = montantSalaireTotal.divide(BigDecimal.valueOf(totalNbJours), BigDecimal.ROUND_HALF_UP);
        }

        for (Pointage p : pointages) {
            double nbJoursEmploye = calculateNombreJoursTravailles(p.getHeuresParJour());
            BigDecimal montantCalcule = montantSalaireParJour.multiply(BigDecimal.valueOf(nbJoursEmploye));

            ModifySalaryDTO salaryDto = new ModifySalaryDTO();
            salaryDto.setMontantSalaire(montantCalcule);

            salaireService.updateSalary(p.getSituation().getId(), p.getEmployeId(), salaryDto);
        }

        return convertToDto(savedPointage);
    }

    @Override
    public void deletePointageBySituationIdAndEmployeId(Long situationId, Long employeId) {
        // Verify the situation exists
        if (!situationRepository.existsById(situationId)) {
            throw new RuntimeException("Situation not found with ID: " + situationId);
        }

        // Find and delete the specific pointage
        pointageRepository.findBySituationIdAndEmployeId(situationId, employeId)
                .ifPresentOrElse(
                        pointage -> {
                            pointageRepository.delete(pointage);
                            log.info("Deleted pointage for situation ID {} and employee ID {}", situationId, employeId);
                        },
                        () -> {
                            throw new RuntimeException(
                                    String.format("Pointage not found for situation ID %d and employee ID %d", situationId, employeId)
                            );
                        }
                );
    }

    @Override
    public PointageResponseDto setAllHeuresToEight(Long pointageId) {
        Pointage pointage = pointageRepository.findById(pointageId)
                .orElseThrow(() -> new RuntimeException("Pointage not found with ID: " + pointageId));

        // Create a new map with the same keys but all values set to 8
        Map<String, Double> updatedHeuresParJour = pointage.getHeuresParJour().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> 8.0 // Set all values to 8
                ));

        // Update the pointage with the new map
        pointage.setHeuresParJour(updatedHeuresParJour);

        // Save the updated pointage
        Pointage savedPointage = pointageRepository.save(pointage);
        updateSalariesForSituation(savedPointage.getSituation().getId());
        return convertToDto(savedPointage);
    }

    @Override
    public PointageResponseDto setAllHeuresToZero(Long pointageId) {
        Pointage pointage = pointageRepository.findById(pointageId)
                .orElseThrow(() -> new RuntimeException("Pointage not found with ID: " + pointageId));

        // Create a new map with the same keys but all values set to 0
        Map<String, Double> updatedHeuresParJour = pointage.getHeuresParJour().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> 0.0 // Set all values to 0
                ));

        // Update the pointage with the new map
        pointage.setHeuresParJour(updatedHeuresParJour);

        // Save the updated pointage
        Pointage savedPointage = pointageRepository.save(pointage);
        updateSalariesForSituation(savedPointage.getSituation().getId());
        return convertToDto(savedPointage);
    }

    private void updateSalariesForSituation(Long situationId) {
        List<Pointage> pointages = pointageRepository.findBySituationId(situationId);
        double totalNbJours = getSumNombreJoursTravaillesBySituationId(situationId);
        BigDecimal montantSalaireTotal = situationService.getSituationById(situationId).getMontantSalaire();
        BigDecimal montantSalaireParJour = BigDecimal.ZERO;
        if (totalNbJours > 0) {
            montantSalaireParJour = montantSalaireTotal.divide(BigDecimal.valueOf(totalNbJours), BigDecimal.ROUND_HALF_UP);
        }

        for (Pointage p : pointages) {
            double nbJoursEmploye = calculateNombreJoursTravailles(p.getHeuresParJour());
            BigDecimal montantCalcule = montantSalaireParJour.multiply(BigDecimal.valueOf(nbJoursEmploye));
            ModifySalaryDTO salaryDto = new ModifySalaryDTO();
            salaryDto.setMontantSalaire(montantCalcule);
            salaireService.updateSalary(p.getSituation().getId(), p.getEmployeId(), salaryDto);
        }
    }

}
