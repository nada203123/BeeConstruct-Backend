package com.example.offreservice.repositories;

import com.example.offreservice.model.HistoriqueEvenement;
import com.example.offreservice.model.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface HistoriqueRepository extends JpaRepository<HistoriqueEvenement, Long>, JpaSpecificationExecutor<HistoriqueEvenement>  {
    List<HistoriqueEvenement> findByOffreIdOrderByCreatedAtDesc(Long offreId);
}
