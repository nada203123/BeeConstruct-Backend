package com.dpc.chantierservice.repositories;

import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.Situation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SituationRepository extends JpaRepository<Situation, Long>  {
    Optional<Situation> findById(Long chantierId);
    List<Situation> findByChantier_Id(Long chantierId);
    @Query("SELECT COALESCE(SUM(s.montantGlobal), 0) FROM Situation s WHERE s.chantier.id = :chantierId")
    BigDecimal sumMontantGlobalByChantierId(@Param("chantierId") Long id);

    List<Situation> findByChantierId(Long chantierId);

    List<Situation> findByChantier(Chantier chantier);

    List<Situation> findByChantierIdOrderByDateSituation(Long chantierId);

    @Query("SELECT COALESCE(SUM(s.montantGlobal), 0) FROM Situation s WHERE s.chantier.id = :chantierId AND s.id != :excludeSituationId")
    BigDecimal sumMontantGlobalByChantierIdExcludingSituation(@Param("chantierId") Long chantierId,
                                                              @Param("excludeSituationId") Long excludeSituationId);


    @Query("SELECT s FROM Situation s WHERE s.chantier.id = :chantierId AND s.id != :excludeSituationId ORDER BY s.dateSituation")
    List<Situation> findByChantierIdAndIdNotOrderByDateSituation(@Param("chantierId") Long chantierId,
                                                                 @Param("excludeSituationId") Long excludeSituationId);
}
