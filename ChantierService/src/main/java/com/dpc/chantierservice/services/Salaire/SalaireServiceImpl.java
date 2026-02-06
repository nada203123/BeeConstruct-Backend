package com.dpc.chantierservice.services.Salaire;

import com.dpc.chantierservice.dto.ModifySalaryDTO;
import com.dpc.chantierservice.dto.SalaryDTO;
import com.dpc.chantierservice.model.Salaire;
import com.dpc.chantierservice.repositories.SalaryRepository;
import com.dpc.chantierservice.repositories.SituationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaireServiceImpl implements SalaireService{
    private final SalaryRepository salaireRepository;
    private final SituationRepository situationRepository;

    public SalaireServiceImpl(SalaryRepository salaireRepository, SituationRepository situationRepository) {
        this.salaireRepository = salaireRepository;
        this.situationRepository = situationRepository;
    }

    @Override
    public Salaire addSalary(Salaire salaire) {
        boolean exists = salaireRepository.existsBySituationIdAndEmployeeId(salaire.getSituationId(), salaire.getEmployeeId());
        if (exists) {
            throw new IllegalArgumentException("Salary for this situationId and employeeId already exists.");
        }
        return salaireRepository.save(salaire);
    }

    @Override
    public void deleteBySituationIdAndEmployeeId(Long situationId, Long employeeId) {
        // Verify the situation exists
        if (!situationRepository.existsById(situationId)) {
            throw new RuntimeException("Situation not found with ID: " + situationId);
        }

        // Find and delete the specific salary
        salaireRepository.findBySituationIdAndEmployeeId(situationId, employeeId)
                .ifPresentOrElse(
                        salaire -> {
                            salaireRepository.delete((Salaire) salaire);

                        },
                        () -> {
                            throw new RuntimeException(
                                    String.format("Salary not found for situation ID %d and employee ID %d", situationId, employeeId)
                            );
                        }
                );
    }



    public class SalaireMapper {
        public static SalaryDTO toDTO(Salaire salaire) {
            SalaryDTO dto = new SalaryDTO();
            dto.setId(salaire.getId());
            dto.setSituationId(salaire.getSituationId());
            dto.setEmployeeId(salaire.getEmployeeId());
            dto.setMontantSalaire(salaire.getMontantSalaire());
            dto.setAvance(salaire.getAvance());
            dto.setCotisation(salaire.getCotisation());
            dto.setLoyer(salaire.getLoyer());
            return dto;
        }

        public static Salaire toEntity(SalaryDTO dto) {
            Salaire salaire = new Salaire();
            salaire.setId(dto.getId());
            salaire.setSituationId(dto.getSituationId());
            salaire.setEmployeeId(dto.getEmployeeId());
            salaire.setMontantSalaire(dto.getMontantSalaire());
            salaire.setAvance(salaire.getAvance());
            salaire.setCotisation(salaire.getCotisation());
            salaire.setLoyer(salaire.getLoyer());
            return salaire;
        }
    }

    public class ModifySalaireMapper {
        public static ModifySalaryDTO toDTO(Salaire salaire) {
            ModifySalaryDTO dto = new ModifySalaryDTO();
            dto.setMontantSalaire(salaire.getMontantSalaire());
            return dto;
        }

        public static Salaire toEntity(ModifySalaryDTO dto) {
            Salaire salaire = new Salaire();
            salaire.setMontantSalaire(dto.getMontantSalaire());
            return salaire;
        }
    }

@Override
    public List<SalaryDTO> getAllSalaries() {
        return salaireRepository.findAll().stream()
                .map(SalaireMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryDTO> getSalariesBySituationId(Long situationId) {
        return salaireRepository.findBySituationId(situationId).stream()
                .map(SalaireMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ModifySalaryDTO updateSalary(Long situationId, Long employeeId, ModifySalaryDTO updatedSalaire) {

        Salaire existingSalaire =  salaireRepository.findBySituationIdAndEmployeeId(situationId, employeeId)
                .orElseThrow(() ->
                        new RuntimeException(String.format(
                                "Salary not found for situation ID %d and employee ID %d", situationId, employeeId))
                );

        existingSalaire.setMontantSalaire(updatedSalaire.getMontantSalaire());
        Salaire saved = salaireRepository.save(existingSalaire);
        return ModifySalaireMapper.toDTO(saved);
    }

    @Override
    public SalaryDTO addRetenue(Long situationId, Long employeeId, String typeRetenue, Double montant) {
        Salaire salaire = salaireRepository.findBySituationIdAndEmployeeId(situationId, employeeId)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Salary not found for situation ID %d and employee ID %d", situationId, employeeId)
                ));

        switch (typeRetenue.toLowerCase()) {
            case "avance":
                salaire.setAvance(BigDecimal.valueOf(montant));
                break;
            case "loyer":
                salaire.setLoyer(BigDecimal.valueOf(montant));
                break;
            case "cotisation":
                salaire.setCotisation(BigDecimal.valueOf(montant));
                break;
            default:
                throw new IllegalArgumentException("Type de retenue invalide : " + typeRetenue);
        }

        Salaire saved = salaireRepository.save(salaire);
        return SalaireMapper.toDTO(saved);
    }










}
