package com.dpc.chantierservice.services.Pointage;

import com.dpc.chantierservice.dto.CreatePointageDto;
import com.dpc.chantierservice.dto.PointageResponseDto;
import com.dpc.chantierservice.model.Pointage;

import java.util.List;

public interface PointageService {
    PointageResponseDto createPointage(CreatePointageDto createPointageDto);

    List<PointageResponseDto> getAllPointages();

    PointageResponseDto getPointageById(Long id);

    PointageResponseDto getPointageBySituationAndEmploye(Long situationId, Long employeId);

    List<PointageResponseDto> getPointagesBySituationId(Long situationId);

    Double getSumNombreJoursTravaillesBySituationId(Long situationId);

    PointageResponseDto updateHeuresJour(Long pointageId, String date, Double heures);

    void deletePointageBySituationIdAndEmployeId(Long situationId, Long employeId);

    PointageResponseDto setAllHeuresToEight(Long pointageId);

    PointageResponseDto setAllHeuresToZero(Long pointageId);
}
