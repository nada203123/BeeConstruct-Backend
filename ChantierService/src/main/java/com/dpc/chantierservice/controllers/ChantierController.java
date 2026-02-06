package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.CreateChantierRequest;
import com.dpc.chantierservice.dto.UpdateChantierRequest;
import com.dpc.chantierservice.model.Chantier;
import com.dpc.chantierservice.model.ChantierStatus;
import com.dpc.chantierservice.services.Chantier.ChantierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chantiers")

public class ChantierController {

    private final ChantierService chantierService;
    public ChantierController(ChantierService chantierService) {
        this.chantierService = chantierService;
    }

    @PostMapping
    public ResponseEntity<Chantier> createChantier(@RequestBody CreateChantierRequest request) {
        Chantier newChantier = chantierService.createChantier(request);
        return ResponseEntity.ok(newChantier);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chantier> getChantierById(@PathVariable Long id) {
        Chantier chantier= chantierService.getChantierById(id);
        return ResponseEntity.ok(chantier);
    }

    @GetMapping
    public ResponseEntity<List<Chantier>> getAllChantiers() {
        List<Chantier> chantiers = chantierService.getAllChantiers();
        return ResponseEntity.ok(chantiers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chantier> updateChantier(
            @PathVariable Long id,
            @RequestBody UpdateChantierRequest request) {

        Chantier updatedChantier = chantierService.updateChantier(id, request);
        return ResponseEntity.ok(updatedChantier);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Chantier> updateChantierStatus(@PathVariable Long id, @RequestParam ChantierStatus status) {

        Chantier updatedChantier = chantierService.updateChantierStatus(id, status);
        return ResponseEntity.ok(updatedChantier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChantier(@PathVariable Long id) {
        chantierService.deleteChantier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-offre/{offreId}")
    public ResponseEntity<Chantier> getChantierByOffreId(@PathVariable Long offreId) {
        Chantier chantier = chantierService.getChantierByOffreId(offreId);
        return ResponseEntity.ok(chantier);
    }

}
