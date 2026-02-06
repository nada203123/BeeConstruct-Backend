package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.CreatePointageDto;
import com.dpc.chantierservice.dto.PointageResponseDto;
import com.dpc.chantierservice.model.Pointage;
import com.dpc.chantierservice.services.Document.DocumentService;
import com.dpc.chantierservice.services.Pointage.PointageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pointage")
@RequiredArgsConstructor
@Slf4j
public class PointageController {

    private final PointageService pointageService;

    @PostMapping
    public ResponseEntity<PointageResponseDto> createPointage(@Valid @RequestBody CreatePointageDto createPointageDto) {
        PointageResponseDto createdPointage = pointageService.createPointage(createPointageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPointage);
    }

    @GetMapping
    public ResponseEntity<List<PointageResponseDto>> getAllPointages() {
        List<PointageResponseDto> pointages = pointageService.getAllPointages();
        return ResponseEntity.ok(pointages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PointageResponseDto> getPointageById(@PathVariable Long id) {
        PointageResponseDto pointageDto = pointageService.getPointageById(id);
        return ResponseEntity.ok(pointageDto);
    }

    @GetMapping("/by-situation-employe")
    public ResponseEntity<PointageResponseDto> getPointageBySituationAndEmploye(
            @RequestParam Long situationId,
            @RequestParam Long employeId) {
        PointageResponseDto pointage = pointageService.getPointageBySituationAndEmploye(situationId, employeId);
        return ResponseEntity.ok(pointage);
    }

    @GetMapping("/situation/{situationId}")
    public ResponseEntity<List<PointageResponseDto>> getPointagesBySituationId(@PathVariable Long situationId) {
        List<PointageResponseDto> pointages = pointageService.getPointagesBySituationId(situationId);
        return ResponseEntity.ok(pointages);
    }

    @GetMapping("/situation/{situationId}/total-jours-travailles")
    public ResponseEntity<Double> getTotalNombreJoursTravaillesBySituationId(@PathVariable Long situationId) {
        Double total = pointageService.getSumNombreJoursTravaillesBySituationId(situationId);
        return ResponseEntity.ok(total);
    }


    @PutMapping("/{id}/heures/{date}")
    public ResponseEntity<PointageResponseDto> updateHeuresJour(
            @PathVariable Long id,
            @PathVariable String date,
            @RequestBody Double heures) {
        try {
            PointageResponseDto response = pointageService.updateHeuresJour(id, date, heures);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour des heures: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{pointageId}/set-all-to-eight")
    public ResponseEntity<PointageResponseDto> setAllHeuresToEight(
            @PathVariable Long pointageId) {
        PointageResponseDto updatedPointage = pointageService.setAllHeuresToEight(pointageId);
        return ResponseEntity.ok(updatedPointage);
    }

    @PutMapping("/{pointageId}/set-all-to-zero")
    public ResponseEntity<PointageResponseDto> setAllHeuresToZero(
            @PathVariable Long pointageId) {
        PointageResponseDto updatedPointage = pointageService.setAllHeuresToZero(pointageId);
        return ResponseEntity.ok(updatedPointage);
    }

    @DeleteMapping("/by-situation-employe")
    public void deletePointageBySituationAndEmploye(
            @RequestParam Long situationId,
            @RequestParam Long employeId) {
        pointageService.deletePointageBySituationIdAndEmployeId(situationId, employeId);
    }
}
