package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.CommandeRequestDTO;
import com.dpc.chantierservice.dto.CommandeResponseDTO;
import com.dpc.chantierservice.services.Marchandises.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponseDTO> addCommande(@RequestBody CommandeRequestDTO commandeRequestDTO) {
        CommandeResponseDTO createdCommande = commandeService.addCommande(commandeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCommande);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> updateCommande(@PathVariable Long id, @RequestBody CommandeRequestDTO commandeRequestDTO) {
        CommandeResponseDTO updatedCommande = commandeService.updateCommande(id, commandeRequestDTO);
        return ResponseEntity.ok(updatedCommande);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> getCommandeById(@PathVariable Long id) {
        CommandeResponseDTO commande = commandeService.getCommandeById(id);
        return ResponseEntity.ok(commande);
    }

    @GetMapping("/chantier/{chantierId}/situation/{situationId}")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByChantierIdAndSituationId(
            @PathVariable Long chantierId,
            @PathVariable Long situationId) {
        List<CommandeResponseDTO> commandes = commandeService.getCommandesByChantierIdAndSituationId(chantierId, situationId);
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/chantier/{chantierId}/total-ttc")
    public ResponseEntity<Double> getTotalTTCByChantierId(@PathVariable Long chantierId) {
        Double totalTTC = commandeService.getTotalTTCByChantierId(chantierId);
        return ResponseEntity.ok(totalTTC);
    }

    @GetMapping("/chantier/{chantierId}/situation/{situationId}/total-ttc")
    public ResponseEntity<Double> getTotalTTCByChantierIdAndSituationId(
            @PathVariable Long chantierId,
            @PathVariable Long situationId) {
        Double totalTTC = commandeService.getTotalTTCByChantierIdAndSituationId(chantierId, situationId);
        return ResponseEntity.ok(totalTTC);
    }


    @GetMapping("/fournisseurs")
    public ResponseEntity<List<String>> getDistinctFournisseurs() {
        List<String> fournisseurs = commandeService.getDistinctFournisseurs();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/chantier/{chantierId}")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByChantierId(@PathVariable Long chantierId) {
        List<CommandeResponseDTO> commandes = commandeService.getCommandesByChantierId(chantierId);
        return ResponseEntity.ok(commandes);
    }

}