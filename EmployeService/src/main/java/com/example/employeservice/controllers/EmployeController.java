package com.example.employeservice.controllers;

import com.example.employeservice.dto.requests.UpdateEmployeRequest;
import com.example.employeservice.dto.requests.addEmployeRequest;
import com.example.employeservice.model.Employe;
import com.example.employeservice.services.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @PostMapping
    public ResponseEntity<Employe> addClient(@Valid @RequestBody addEmployeRequest request) {
        Employe employe = employeService.addEmploye(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employe);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Employe>> getAllActiveEmployes() {
        List<Employe> activeEmployes = employeService.getAllActiveEmployes();
        return ResponseEntity.ok(activeEmployes);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<Employe>> getAllArchivedEmployes() {
        List<Employe> archivedEmployes = employeService.getAllArchivedEmployes();
        return ResponseEntity.ok(archivedEmployes);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Employe> archiveEmploye(@PathVariable Long id) {
        Employe archivedEmploye = employeService.archiveEmploye(id);
        return ResponseEntity.ok(archivedEmploye);
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Employe> restoreEmploye(@PathVariable Long id) {
        Employe restoredEmploye = employeService.restoreEmploye(id);
        return ResponseEntity.ok(restoredEmploye);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employe> updateEmploye(
            @PathVariable Long id,
            @RequestBody UpdateEmployeRequest request) {
        Employe updatedEmploye = employeService.updateEmploye(id, request);
        return ResponseEntity.ok(updatedEmploye);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteEmploye(@PathVariable Long id) {
        employeService.deleteEmploye(id);
        return ResponseEntity.noContent().build();
    }




    @GetMapping("/{id}")
    public ResponseEntity<Employe> getEmployeById(@PathVariable Long id) {
        Employe employe = employeService.getEmployeById(id);
        return ResponseEntity.ok(employe);
    }

    @GetMapping("/by-ids")
    public List<Employe> getEmployesByIds(@RequestParam List<Long> ids) {
        return employeService.getEmployesByIds(ids);
    }

}
