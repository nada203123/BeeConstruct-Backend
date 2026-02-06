package com.example.clientsservice.repositories;

import com.example.clientsservice.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {


    List<Client> findByArchivedFalse();
    List<Client> findByArchivedTrue();
    // In ClientRepository


    boolean existsByNomSociete(String nomSociete);

    boolean existsByTelephoneDirecteur(String telephoneDirecteur);
    List<Client> findByNomSocieteIgnoreCase(String nomSociete);
}
