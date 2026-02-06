package com.example.offreservice.controllers;


import com.example.offreservice.dto.ClientDTO;

import com.example.offreservice.dto.request.CreateOffreRequest;
import com.example.offreservice.dto.request.DevisFileDownload;
import com.example.offreservice.dto.request.HistoryRequestDTO;
import com.example.offreservice.dto.response.OffreDTO;

import java.util.UUID;
import java.util.stream.Collectors;

import com.example.offreservice.model.*;
import com.example.offreservice.services.OffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    @PostMapping
    public ResponseEntity<Offre> createOffre(@RequestBody CreateOffreRequest request, @RequestHeader("Authorization") String authToken) {
        Offre offre = offreService.createOffre(request,authToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(offre);
    }

    @GetMapping
    public List<Offre> getAllOffres() {
        return offreService.getAllOffres();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offre> getOffreById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getOffreById(id));
    }





    @GetMapping("/by-status")
    public List<Offre> getOffresByStatut(@RequestParam StatutOffre statut) {
        return offreService.getOffresByStatut(statut);
    }




    @GetMapping("/search")
    public List<Offre> searchOffres(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) StatutOffre statut,
            @RequestParam(required = false) TypeOffre type) {

        return offreService.searchOffres(clientId, localisation, statut,type);
    }

    @GetMapping("/localisations")
    public List<String> getAllLocalisations() {
        return offreService.getAllLocalisations();
    }

    @GetMapping("/{id}/client")
    public ResponseEntity<ClientDTO> getClientForOffre(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authToken) {
        ClientDTO client = offreService.getClientInfoForOffre(id, authToken);
        return ResponseEntity.ok(client);
    }





    @GetMapping("/distinct-clients")
    public ResponseEntity<List<ClientDTO>> getAllDistinctClients(
            @RequestHeader("Authorization") String authToken) {
        List<ClientDTO> clients = offreService.getAllDistinctClients(authToken);
        return ResponseEntity.ok(clients);
    }



    @PutMapping("/{id}")
    public ResponseEntity<Offre> updateOffre(
            @PathVariable Long id,
            @RequestBody CreateOffreRequest request,
            @RequestHeader("Authorization") String authToken) {
        Offre updatedOffre = offreService.updateOffre(id, request, authToken);
        return ResponseEntity.ok(updatedOffre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long id) {
        offreService.deleteOffre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveOffre(@PathVariable Long id) {
        offreService.archiveOffre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreOffre(@PathVariable Long id) {
        offreService.restoreOffre(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/archived")
    public ResponseEntity<List<Offre>> getAllArchivedOffres() {
        return ResponseEntity.ok(offreService.getAllArchivedOffres());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Offre>> getActiveOffres() {
        return ResponseEntity.ok(offreService.getActiveOffres());
    }


  /* @GetMapping("/active")
    public ResponseEntity<List<OffreDTO>> getActiveOffres(@RequestHeader("Authorization") String authToken) {
        List<OffreDTO> activeOffres = offreService.getActiveOffres().stream()
                .map(offre -> {
                    OffreDTO dto = new OffreDTO();
                    dto.setId(offre.getId());
                    dto.setTitre(offre.getTitre());
                    dto.setLocalisation(offre.getLocalisation());
                    dto.setStatutOffre(offre.getStatutOffre());
                    dto.setArchived(offre.isArchived());
                    dto.setClientId(offre.getClientId());
                    // Fetch the client details and set the client field
                    ClientDTO client = offreService.getClientInfoForOffre(offre.getId(), authToken);
                    dto.setClient(client);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeOffres);
    }
*/

    @GetMapping("/active/{statut}")
    public List<Offre> getActiveOffresByStatut(@PathVariable StatutOffre statut) {
        return offreService.getActiveOffresByStatut(statut);
    }



    @GetMapping("/activeClients")
    public ResponseEntity<List<ClientDTO>> getAllActiveClients(
            @RequestHeader("Authorization") String authToken) {
        try {
            List<ClientDTO> clients = offreService.getAllActiveClients(authToken);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Offre> updateOffreStatut(
            @PathVariable Long id,
            @RequestBody StatutOffre newStatut) {

        Offre updatedOffre = offreService.updateOffreStatut(id, newStatut);
        return ResponseEntity.ok(updatedOffre);
    }

    @PostMapping(value = "/history", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HistoriqueEvenement> addHistory(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("offreId") String offreId,
            @RequestPart("comment") String comment) {

        HistoryRequestDTO request = new HistoryRequestDTO();
        request.setOffreId(Long.parseLong(offreId));
        request.setComment(comment);
        request.setFile(file);
        HistoriqueEvenement history = offreService.addHistory(request);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{offreId}/history")
    public ResponseEntity<List<HistoriqueEvenement>> getHistory(
            @PathVariable Long offreId) {
        List<HistoriqueEvenement> history = offreService.getHistoryByOffreId(offreId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDevisFile(@PathVariable Long id) {
        DevisFileDownload download = offreService.downloadDevisFile(id);
        InputStreamResource resource = new InputStreamResource(download.getInputStream());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"")
                .body(resource);
    }



    @GetMapping("/check-stale")
    @CrossOrigin
    public void checkAndNotifyStaleOffres() {
         offreService.checkAndNotifyStaleOffres();
    }
}
