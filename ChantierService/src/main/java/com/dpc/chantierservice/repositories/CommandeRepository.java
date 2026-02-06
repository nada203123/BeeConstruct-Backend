package com.dpc.chantierservice.repositories;

import com.dpc.chantierservice.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByChantierId(Long chantierId);

    @Query("SELECT DISTINCT c.nomFournisseur FROM Commande c")
    List<String> findDistinctFournisseurs();

    List<Commande> findByChantierIdAndSituationId(Long chantierId, Long situationId);
}