package com.dpc.chantierservice.repositories;

import com.dpc.chantierservice.model.Salaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salaire, Long> {


    boolean existsBySituationIdAndEmployeeId(Long situationId, Long employeeId);

    long deleteBySituationIdAndEmployeeId(Long situationId, Long employeeId);

    Optional<Salaire> findBySituationIdAndEmployeeId(Long situationId, Long employeeId);

    List<Salaire> findBySituationId(Long situationId);

}