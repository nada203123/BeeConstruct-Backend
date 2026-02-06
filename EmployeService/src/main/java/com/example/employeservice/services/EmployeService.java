package com.example.employeservice.services;

import com.example.employeservice.dto.requests.UpdateEmployeRequest;
import com.example.employeservice.dto.requests.addEmployeRequest;
import com.example.employeservice.model.Employe;

import java.util.List;

public interface EmployeService {
    Employe addEmploye(addEmployeRequest request);

    List<Employe> getAllActiveEmployes();

    List<Employe> getAllArchivedEmployes();

    Employe archiveEmploye(Long id);

    Employe restoreEmploye(Long id);

    Employe updateEmploye(Long id, UpdateEmployeRequest request);

    void deleteEmploye(Long id);




    Employe getEmployeById(Long id);

    List<Employe> getEmployesByIds(List<Long> ids);
}
