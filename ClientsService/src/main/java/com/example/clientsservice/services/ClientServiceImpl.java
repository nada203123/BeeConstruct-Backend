package com.example.clientsservice.services;

import com.example.clientsservice.dto.requests.UpdateClientRequest;
import com.example.clientsservice.dto.responses.ClientDTO;
import com.example.clientsservice.dto.requests.addClientRequest;
import com.example.clientsservice.model.Client;
import com.example.clientsservice.repositories.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;


import java.util.List;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService{
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository ) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client addClient(addClientRequest request) {

        if (clientRepository.existsByNomSociete(request.getNomSociete())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom de la société existe déjà.");
        }

        if (clientRepository.existsByTelephoneDirecteur(request.getTelephoneDirecteur())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le numéro de téléphone du directeur existe déjà.");
        }


        // Create and save client
        return clientRepository.save(Client.builder()
                .nomSociete(request.getNomSociete())
                .siegeSocial(request.getSiegeSocial())
                .adresse(request.getAdresse())
                .nomDirecteur(request.getNomDirecteur())
                .prenomDirecteur(request.getPrenomDirecteur())
                .telephoneDirecteur(request.getTelephoneDirecteur())
                .archived(false)
                .build());
    }


    @Override
    public List<ClientDTO> getAllActiveClients() {
        return clientRepository.findByArchivedFalse().stream()
                .map(client -> {
                    ClientDTO clientDTO = new ClientDTO();
                    clientDTO.setId(client.getId());
                    clientDTO.setNomSociete(client.getNomSociete());
                    clientDTO.setSiegeSocial(client.getSiegeSocial());
                    clientDTO.setAdresse(client.getAdresse());
                    clientDTO.setNomDirecteur(client.getNomDirecteur());
                    clientDTO.setPrenomDirecteur(client.getPrenomDirecteur());
                    clientDTO.setTelephoneDirecteur(client.getTelephoneDirecteur());
                    return clientDTO;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<Client> getAllArchivedClients() {
        return clientRepository.findByArchivedTrue();
    }

    @Override
    public Client archiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        if (client.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client is already archived");
        }

        client.setArchived(true);
        return clientRepository.save(client);
    }

    @Override
    public Client restoreClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        if (!client.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client is not archived");
        }

        client.setArchived(false);
        return clientRepository.save(client);
    }

    @Override
    public Client updateClient(Long id, UpdateClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        // Check if email is being changed and if new email already exists
        if (!client.getNomSociete().equals(request.getNomSociete())) {
            if (clientRepository.existsByNomSociete(request.getNomSociete())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom de la société existe déjà.");
            }
        }

        if (!client.getTelephoneDirecteur().equals(request.getTelephoneDirecteur())) {
            if (clientRepository.existsByTelephoneDirecteur(request.getTelephoneDirecteur())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le numéro de téléphone du directeur existe déjà.");
            }
        }

        // Update client fields
        client.setNomSociete(request.getNomSociete());
        client.setSiegeSocial(request.getSiegeSocial());
        client.setAdresse(request.getAdresse());
        client.setNomDirecteur(request.getNomDirecteur());
        client.setPrenomDirecteur(request.getPrenomDirecteur());
        client.setTelephoneDirecteur(request.getTelephoneDirecteur());

        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
        clientRepository.delete(client);
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

}
