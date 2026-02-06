package com.example.clientsservice.services;

import com.example.clientsservice.dto.requests.UpdateClientRequest;
import com.example.clientsservice.dto.requests.addClientRequest;
import com.example.clientsservice.dto.responses.ClientDTO;
import com.example.clientsservice.dto.responses.ClientDTO;
import com.example.clientsservice.model.Client;

import java.util.List;

public interface ClientService {
    Client addClient(addClientRequest request);

    List<ClientDTO> getAllActiveClients();

    List<Client> getAllArchivedClients();

    Client archiveClient(Long id);

    Client restoreClient(Long id);

    Client updateClient(Long id, UpdateClientRequest request);

    void deleteClient(Long id);

    Client getClientById(Long id);





}
