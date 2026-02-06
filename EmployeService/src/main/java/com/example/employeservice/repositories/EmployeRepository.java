package com.example.employeservice.repositories;

import com.example.employeservice.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    boolean existsByTelephone(String telephone);


    List<Employe> findByArchivedFalse();
    List<Employe> findByArchivedTrue();

    boolean existsByRib(String rib);





}
