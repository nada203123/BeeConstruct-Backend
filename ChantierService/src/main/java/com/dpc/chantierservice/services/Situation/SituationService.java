package com.dpc.chantierservice.services.Situation;

import com.dpc.chantierservice.dto.SituationRequest;
import com.dpc.chantierservice.dto.SituationResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface SituationService {


    @Transactional
    SituationResponse createSituation(SituationRequest request);

    SituationResponse getSituationById(Long id);

    List<SituationResponse> getAllSituations();

    List<SituationResponse> getSituationsByChantierId(Long chantierId);


    void deleteSituation(Long id);


    @Transactional
    SituationResponse updateSituation(Long situationId, SituationRequest request);
}
