package com.example.clientsservice.controllers;

import com.example.clientsservice.dto.requests.UpdateClientRequest;
import com.example.clientsservice.dto.requests.addClientRequest;
import com.example.clientsservice.dto.responses.ClientDTO;
import com.example.clientsservice.model.Client;
import com.example.clientsservice.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> addClient(@Valid @RequestBody addClientRequest request) {
        Client client = clientService.addClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ClientDTO>> getAllActiveClients() {
        List<ClientDTO> activeClients = clientService.getAllActiveClients();
        return ResponseEntity.ok(activeClients);
    }


    @GetMapping("/archived")
    public ResponseEntity<List<Client>> getAllArchivedClients() {
        List<Client> archivedClients = clientService.getAllArchivedClients();
        return ResponseEntity.ok(archivedClients);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Client> archiveClient(@PathVariable Long id) {
        Client archivedClient = clientService.archiveClient(id);
        return ResponseEntity.ok(archivedClient);
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Client> restoreClient(@PathVariable Long id) {
        Client restoredClient = clientService.restoreClient(id);
        return ResponseEntity.ok(restoredClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id,
            @RequestBody UpdateClientRequest request) {
        Client updatedClient = clientService.updateClient(id, request);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Client clientDTO = clientService.getClientById(id);
        return ResponseEntity.ok(clientDTO);
    }






}
