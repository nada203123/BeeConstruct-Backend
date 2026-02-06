package com.dpc.chantierservice.services.Chantier;

import com.dpc.chantierservice.dto.CreateChantierRequest;
import com.dpc.chantierservice.dto.UpdateChantierRequest;
import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.ChantierStatus;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ChantierService {
    Chantier createChantier(CreateChantierRequest request);

    Chantier getChantierById(Long id);


    List<Chantier> getAllChantiers();

    Chantier updateChantier(Long id, UpdateChantierRequest request);

    Chantier updateChantierStatus(Long id, ChantierStatus newStatus);

    @Transactional
    void deleteChantier(Long id);


    Chantier getChantierByOffreId(Long offreId);
}
