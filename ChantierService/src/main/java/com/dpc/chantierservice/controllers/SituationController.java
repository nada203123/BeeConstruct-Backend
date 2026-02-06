package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.SituationRequest;
import com.dpc.chantierservice.dto.SituationResponse;
import com.dpc.chantierservice.services.Chantier.ChantierService;
import com.dpc.chantierservice.services.Situation.SituationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/situations")
public class SituationController {

    private final SituationService situationService;
    public SituationController(SituationService situationService) {
        this.situationService = situationService;
    }

    @PostMapping
    public ResponseEntity<SituationResponse> createSituation(@Valid @RequestBody SituationRequest request) {
        SituationResponse response = situationService.createSituation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SituationResponse> getSituationById(@PathVariable Long id) {
        SituationResponse response = situationService.getSituationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SituationResponse>> getAllSituations() {
        List<SituationResponse> responses = situationService.getAllSituations();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/chantier/{chantierId}")
    public ResponseEntity<List<SituationResponse>> getSituationsByChantierId(@PathVariable Long chantierId) {
        List<SituationResponse> responses = situationService.getSituationsByChantierId(chantierId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SituationResponse> updateSituation(
            @PathVariable Long id,
            @RequestBody SituationRequest situationRequest) {


            SituationResponse updatedSituation = situationService.updateSituation(id, situationRequest);
        return ResponseEntity.ok(updatedSituation);

    }

    @DeleteMapping("/{id}")
    public void deleteSituation(@PathVariable Long id) {
        situationService.deleteSituation(id);
    }
}
