package com.example.offreservice.repositories;

import com.example.offreservice.model.Offre;
import com.example.offreservice.model.StatutOffre;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long>, JpaSpecificationExecutor<Offre> {

    List<Offre> findByClientId(Long id);
    List<Offre> findByLocalisation(String localisation);
    List<Offre> findByStatutOffre(StatutOffre statutOffre);

    @Query("SELECT DISTINCT o.localisation FROM Offre o WHERE o.localisation IS NOT NULL")
    List<String> findAllDistinctLocalisations();

    @Query("SELECT DISTINCT o.clientId FROM Offre o")
    List<Long> findDistinctClientIds();

   // List<Offre> findByArchivedTrue();
   @Query("SELECT o FROM Offre o WHERE o.archived = true ORDER BY o.dateDemande DESC")
   List<Offre> findByArchivedTrue();

    @Query("SELECT o FROM Offre o WHERE o.archived = false ORDER BY o.dateDemande DESC")
    List<Offre> findByArchivedFalse();

   // List<Offre> findByArchivedFalse();
    @Query("SELECT DISTINCT o.clientId FROM Offre o WHERE o.archived = false")
    List<Long> findDistinctClientIdsByArchivedFalse();


    @Query("SELECT o FROM Offre o WHERE o.statutOffre = :statutOffre AND o.archived = false ORDER BY o.dateDemande DESC")
    List<Offre> findByStatutOffreAndArchivedFalse(@Param("statutOffre") StatutOffre statutOffre);
}
