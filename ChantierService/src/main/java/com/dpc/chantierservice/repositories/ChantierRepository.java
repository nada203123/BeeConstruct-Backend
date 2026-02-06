package com.dpc.chantierservice.repositories;

import com.dpc.chantierservice.model.Chantier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChantierRepository extends JpaRepository<Chantier, Long> {
    boolean existsByOffreId(Long offreId);

    Optional<Chantier> findByOffreId(Long offreId);
}
