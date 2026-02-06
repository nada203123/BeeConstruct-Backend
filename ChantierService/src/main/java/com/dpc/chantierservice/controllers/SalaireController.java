package com.dpc.chantierservice.controllers;

import com.dpc.chantierservice.dto.ModifySalaryDTO;
import com.dpc.chantierservice.dto.SalaryDTO;
import com.dpc.chantierservice.model.Salaire;
import com.dpc.chantierservice.services.Salaire.SalaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salaire")
@RequiredArgsConstructor
@Slf4j
public class SalaireController {

    private final SalaireService salaireService;



    @PostMapping
    public ResponseEntity<Salaire> addSalary(@RequestBody Salaire salaire) {

        return ResponseEntity.ok(salaireService.addSalary(salaire));
    }

    @DeleteMapping("/situation/{situationId}/employee/{employeeId}")
    public ResponseEntity<String> deleteBySituationIdAndEmployeeId(@PathVariable Long situationId,
                                                                   @PathVariable Long employeeId) {
        try {
            salaireService.deleteBySituationIdAndEmployeeId(situationId, employeeId);
            return ResponseEntity.ok("Salary deleted successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete salary.");
        }
    }


    @GetMapping
    public ResponseEntity<List<SalaryDTO>> getAllSalaries() {
        List<SalaryDTO> salaries = salaireService.getAllSalaries();
        return ResponseEntity.ok(salaries);
    }

    @GetMapping("/situation/{situationId}")
    public ResponseEntity<List<SalaryDTO>> getSalariesBySituation(@PathVariable Long situationId) {
        List<SalaryDTO> salaries = salaireService.getSalariesBySituationId(situationId);
        return ResponseEntity.ok(salaries);
    }

    @PutMapping("/{situationId}/{employeeId}")
    public ResponseEntity<ModifySalaryDTO> updateSalary(
            @PathVariable Long situationId,
            @PathVariable Long employeeId,
            @RequestBody ModifySalaryDTO updatedSalaire) {
        try {
            ModifySalaryDTO updated = salaireService.updateSalary(situationId, employeeId, updatedSalaire);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{situationId}/{employeeId}/retenue")
    public ResponseEntity<SalaryDTO> addRetenue(
            @PathVariable Long situationId,
            @PathVariable Long employeeId,
            @RequestParam String type,
            @RequestParam Double montant) {

        try {
            SalaryDTO updatedSalaire = salaireService.addRetenue(situationId, employeeId, type, montant);
            return new ResponseEntity<>(updatedSalaire, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

}
