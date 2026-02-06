package com.example.clientsservice.dto.responses;


import lombok.Data;
@Data

public class ClientDTO {
    private Long id;
    private String nomSociete;
    private String siegeSocial;
    private String adresse;
    private String nomDirecteur;
    private String prenomDirecteur;
    private String telephoneDirecteur;
}
