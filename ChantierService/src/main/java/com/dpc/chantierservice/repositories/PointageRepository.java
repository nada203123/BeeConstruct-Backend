package com.dpc.chantierservice.repositories;

import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointageRepository extends JpaRepository<Pointage, Long> {
    Optional<Pointage> findByEmployeId(Long employeId);

    Optional<Pointage> findBySituationIdAndEmployeId(Long situationId, Long employeId);

    Optional<Object> findByEmployeIdAndSituationId(Long employeId, Long situationId);


    List<Pointage> findBySituationId(Long situationId);
}
