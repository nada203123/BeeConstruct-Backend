package com.dpc.chantierservice.services.Salaire;

import com.dpc.chantierservice.dto.ModifySalaryDTO;
import com.dpc.chantierservice.dto.SalaryDTO;
import com.dpc.chantierservice.model.Salaire;

import java.util.List;

public interface SalaireService {
    Salaire addSalary(Salaire salaire);

    void deleteBySituationIdAndEmployeeId(Long situationId, Long employeeId);

    List<SalaryDTO> getAllSalaries();

    List<SalaryDTO> getSalariesBySituationId(Long situationId);


    ModifySalaryDTO updateSalary(Long situationId, Long employeeId, ModifySalaryDTO updatedSalaire);

    SalaryDTO addRetenue(Long situationId, Long employeeId, String typeRetenue, Double montant);
}
