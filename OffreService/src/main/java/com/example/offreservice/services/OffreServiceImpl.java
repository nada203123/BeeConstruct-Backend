package com.example.offreservice.services;


import com.example.offreservice.dto.ChantierDTO;
import com.example.offreservice.dto.ClientDTO;
import com.example.offreservice.dto.request.CreateOffreRequest;
import com.example.offreservice.dto.request.HistoryRequestDTO;
import com.example.offreservice.feign.ChantierFeignClient;
import com.example.offreservice.feign.ClientFeignClient;
import com.example.offreservice.model.HistoriqueEvenement;
import com.example.offreservice.model.Offre;
import com.example.offreservice.model.StatutOffre;
import com.example.offreservice.model.TypeOffre;
import com.example.offreservice.repositories.HistoriqueRepository;
import com.example.offreservice.repositories.OffreRepository;
import com.example.offreservice.specifications.OffreSpecifications;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.example.offreservice.dto.request.*;

@Service
@Slf4j
public class OffreServiceImpl implements OffreService{

    private final OffreRepository offreRepository;
    private final ClientFeignClient clientFeignClient;

    private final FileStorageService fileStorageService;
    private final HistoriqueRepository historiqueRepository;

    private final NotificationService notificationService;

    private final ChantierFeignClient chantierFeignClient;

    public OffreServiceImpl(OffreRepository offreRepository ,ClientFeignClient clientFeignClient,FileStorageService fileStorageService,HistoriqueRepository historiqueRepository,NotificationService notificationService,ChantierFeignClient chantierFeignClient) {
        this.offreRepository = offreRepository;
        this.clientFeignClient = clientFeignClient;
        this.fileStorageService = fileStorageService;
        this.historiqueRepository = historiqueRepository;
        this.notificationService = notificationService;
        this.chantierFeignClient = chantierFeignClient;
    }

    @Override
    public Offre createOffre(CreateOffreRequest request, String authToken) {
        try {

            ClientDTO client = clientFeignClient.getClientById(request.getClientId(), authToken);

            Offre offre = Offre.builder()
                    .titre(request.getTitre())
                    .localisation(request.getLocalisation())
                    .statutOffre(request.getStatutOffre())
                    .clientId(client.getId())
                    .dateDemande(request.getDateDemande())
                    .type(request.getType())
                    .build();

            return offreRepository.save(offre);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found with ID: " + request.getClientId());
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error communicating with client service", e);
        }
    }




    @Override
    public List<Offre> getAllOffres() {
        return offreRepository.findAll();
    }

    @Override
    public Offre getOffreById(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with id: " + id));
    }


    @Override
    public List<Offre> getOffresByStatut(StatutOffre statutOffre) {
        return offreRepository.findByStatutOffre(statutOffre);
    }

    @Override
    public List<Offre> getActiveOffresByStatut(StatutOffre statutOffre) {
        return offreRepository.findByStatutOffreAndArchivedFalse(statutOffre);
    }




    @Override
    public List<Offre> searchOffres(Long clientId, String localisation, StatutOffre statut, TypeOffre type) {
        Specification<Offre> spec = Specification.where(OffreSpecifications.withArchived(false)) // Only active offers
                .and(OffreSpecifications.withClientId(clientId))
                .and(OffreSpecifications.withLocalisation(localisation))
                .and(OffreSpecifications.withStatut(statut))
                .and(OffreSpecifications.withType(type));

        return offreRepository.findAll(spec);
    }


    @Override
    public List<String> getAllLocalisations() {
        return offreRepository.findAllDistinctLocalisations();
    }

    @Override
    public ClientDTO getClientInfoForOffre(Long offreId, String authToken) {
        try {
            // First get the offre to ensure it exists and get the client ID
            Offre offre = offreRepository.findById(offreId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + offreId));

            // Then fetch the client info using Feign client
            return clientFeignClient.getClientById(offre.getClientId(), authToken);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found for offre ID: " + offreId);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error communicating with client service", e);
        }
    }


    @Override
    public List<ClientDTO> getAllDistinctClients(String authToken) {
        try {
            // Get all distinct client IDs from offres
            List<Long> clientIds = offreRepository.findDistinctClientIdsByArchivedFalse();

            // Fetch client details for each ID
            return clientIds.stream()
                    .map(id -> {
                        try {
                            return clientFeignClient.getClientById(id, authToken);
                        } catch (FeignException e) {
                            log.error("Error fetching client with ID: " + id, e);
                            return null; // or handle differently
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting distinct clients", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching clients", e);
        }
    }

    @Override
    public Offre updateOffre(Long id, CreateOffreRequest request, String authToken) {
        try {
            // Verify client exists
            ClientDTO client = clientFeignClient.getClientById(request.getClientId(), authToken);

            // Get existing offre
            Offre existingOffre = offreRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + id));


            existingOffre.setTitre(request.getTitre());
            existingOffre.setLocalisation(request.getLocalisation());
            existingOffre.setStatutOffre(request.getStatutOffre());
            existingOffre.setClientId(client.getId());
            existingOffre.setDateDemande(request.getDateDemande());
            existingOffre.setType(request.getType());

            return offreRepository.save(existingOffre);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found with ID: " + request.getClientId());
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error communicating with client service", e);
        }
    }

    @Override
    public void deleteOffre(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + id));

        offreRepository.delete(offre);
    }

    @Override
    public void archiveOffre(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + id));

        offre.setArchived(true);
        offreRepository.save(offre);
    }

    @Override
    public void restoreOffre(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + id));

        offre.setArchived(false);
        offreRepository.save(offre);
    }

    @Override
    public List<Offre> getAllArchivedOffres() {
        return offreRepository.findByArchivedTrue();
    }

    @Override
    public List<Offre> getActiveOffres() {
        return offreRepository.findByArchivedFalse();
    }

    @Override
    public List<ClientDTO> getAllActiveClients(String authToken) {
        try {
            // Use the Feign client to get all clients directly from the client service
            return clientFeignClient.getAllActiveClients(authToken);
        } catch (FeignException e) {
            log.error("Error fetching all clients from client service", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error communicating with client service",
                    e
            );
        }
    }

    @Override
    public Offre updateOffreStatut(Long id, StatutOffre newStatut) {
        String token = getCurrentUserToken();
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre not found with ID: " + id));

        StatutOffre oldStatut = offre.getStatutOffre();

        if(newStatut == StatutOffre.ACCEPTEE) {
            createChantierFromOffre(offre);
        } else {
            try {
                // Find and delete chantier associated with this offre
                ChantierDTO chantier = chantierFeignClient.getChantierByOffreId(offre.getId(), token);
                if(chantier != null) {
                    chantierFeignClient.deleteChantier(chantier.getId(), token);
                }
            } catch (Exception e) {
                // Handle case where no chantier exists for this offre
                // Log the exception if needed
            }
        }

        offre.setStatutOffre(newStatut);
        Offre updatedOffre = offreRepository.save(offre);
        addStatusChangeHistory(updatedOffre, oldStatut, newStatut);

        return updatedOffre;

    }

    private void addStatusChangeHistory(Offre offre, StatutOffre oldStatut, StatutOffre newStatut) {
        String comment = String.format("Statut changé de '%s' à '%s'",
                oldStatut != null ? oldStatut.name() : "N/A",
                newStatut.name());

        HistoriqueEvenement history = HistoriqueEvenement.builder()
                .offre(offre)
                .comment(comment)
                .isArchived(false)
                .build();

        historiqueRepository.save(history);
    }


    @Override
    public HistoriqueEvenement addHistory(HistoryRequestDTO request) {

        Offre offre = offreRepository.findById(request.getOffreId())
                .orElseThrow(() -> new IllegalArgumentException("offre not found with ID: " + request.getOffreId()));





        HistoriqueEvenement history = HistoriqueEvenement.builder()
                .offre(offre)
                .comment(request.getComment())
                .filePath(offre.getFilePath())
                .originalFileName(offre.getFileName())
                .isArchived(false)
                .build();

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null ||
                    !(originalFileName.toLowerCase().endsWith(".pdf") ||
                            originalFileName.toLowerCase().endsWith(".doc") ||
                            originalFileName.toLowerCase().endsWith(".docx"))) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only PDF and Word documents (.pdf, .doc, .docx) are allowed"
                );
            }

            Map<String, String> fileUploadResult = null;
            try {
                fileUploadResult = fileStorageService.storeFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String storedFileName = fileUploadResult.get("fileName");
            String fileUrl = fileUploadResult.get("fileUrl");

            offre.setFilePath(fileUrl);
            offre.setFileName(originalFileName);
        }


        return historiqueRepository.save(history);
    }

    @Override
    public List<HistoriqueEvenement> getHistoryByOffreId(Long offreId) {

        if (!offreRepository.existsById(offreId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Devis not found with ID: " + offreId
            );
        }
        return historiqueRepository.findByOffreIdOrderByCreatedAtDesc(offreId);
    }

    @Override
    public DevisFileDownload downloadDevisFile(Long offreId) {
        Offre devis = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devis not found with id: " + offreId));

        String filePath = devis.getFilePath();
        String originalFileName = devis.getFileName(); // Get the original filename

        log.info("File path: {}", filePath);
        if (filePath == null || filePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found for devis with id: " + offreId);
        }

        try {
            // Extract the object name (generated filename) from the path
            String objectName = extractObjectNameFromPath(filePath);
            log.info("Extracted object name: {}", objectName);

            InputStream inputStream = fileStorageService.getFile(objectName);
            String contentType = fileStorageService.getContentType(objectName);

            // Use the original filename for download, but the generated name to fetch the file
            return new DevisFileDownload(inputStream, contentType, originalFileName);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving file", e);
        }
    }
    private String extractObjectNameFromPath(String filePath) {
        try {
            URI uri = new URI(filePath);
            String path = uri.getPath();
            // Split on '?' to remove query parameters if present
            String basePath = path.split("\\?")[0];
            String fileName = basePath.substring(basePath.lastIndexOf('/') + 1);
            return fileName;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid file path: " + filePath, e);
        }
    }
@Override
    public void checkAndNotifyStaleOffres() {
        List<Offre> activeOffres = offreRepository.findByArchivedFalse();
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(30);

        activeOffres.forEach(offre -> {
            List<HistoriqueEvenement> history = historiqueRepository.findByOffreIdOrderByCreatedAtDesc(offre.getId());
            if (!history.isEmpty()) {
                LocalDateTime lastUpdate = history.get(0).getCreatedAt();
                if (lastUpdate.isBefore(oneDayAgo)) {
                    notificationService.sendStaleOffreNotification(offre);
                    System.out.println("senddddddddddddddddddddc");
                }
            }
        });
    }


    @Scheduled(fixedRate = 60) // 1 hour
    public void scheduledStaleCheck() {
        checkAndNotifyStaleOffres();
    }

    private String getCurrentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return "Bearer " + jwtAuth.getToken().getTokenValue();
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Cannot get authentication token"
        );
    }

    private void createChantierFromOffre(Offre offre) {
        String token = getCurrentUserToken();
        ClientDTO client = getClientInfoForOffre(offre.getId(),token);
        System.out.println(client);

        CreateChantierRequest request = CreateChantierRequest.builder()
                .offreId(offre.getId())
                .titre(offre.getTitre())
                .localisation(offre.getLocalisation())
                .clientId(offre.getClientId())
                .client(client.getNomSociete())
                .statut("en_cours")
                .progression(0)
                .build();
System.out.println(request);
        try {
            chantierFeignClient.createChantier(request,token);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create chantier for accepted offre"
            );
        }
    }

}
