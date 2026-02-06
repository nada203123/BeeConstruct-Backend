package com.example.offreservice.services;

import com.example.offreservice.dto.ClientDTO;
import com.example.offreservice.dto.request.CreateOffreRequest;
import com.example.offreservice.dto.request.DevisFileDownload;
import com.example.offreservice.dto.request.HistoryRequestDTO;
import com.example.offreservice.model.HistoriqueEvenement;
import com.example.offreservice.model.Offre;
import com.example.offreservice.model.StatutOffre;
import com.example.offreservice.model.TypeOffre;

import java.util.List;

public interface OffreService {



    // Offre createOffre(CreateOffreRequest request);

    Offre createOffre(CreateOffreRequest request, String authToken);

    List<Offre> getAllOffres();

    Offre getOffreById(Long id);


    List<Offre> getOffresByStatut(StatutOffre statutOffre);


    List<Offre> getActiveOffresByStatut(StatutOffre statutOffre);

    List<Offre> searchOffres(Long clientId, String localisation, StatutOffre statut , TypeOffre type);

    List<String> getAllLocalisations();

    ClientDTO getClientInfoForOffre(Long offreId, String authToken);


    List<ClientDTO> getAllDistinctClients(String authToken);

    Offre updateOffre(Long id, CreateOffreRequest request, String authToken);

    void deleteOffre(Long id);

    void archiveOffre(Long id);

    void restoreOffre(Long id);

    List<Offre> getAllArchivedOffres();

    List<Offre> getActiveOffres();


    List<ClientDTO> getAllActiveClients(String authToken);

    Offre updateOffreStatut(Long id, StatutOffre newStatut);

    HistoriqueEvenement addHistory(HistoryRequestDTO request);

    List<HistoriqueEvenement> getHistoryByOffreId(Long offreId);

    DevisFileDownload downloadDevisFile(Long offreId);

    void checkAndNotifyStaleOffres();
}
