package com.example.clientsservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class addClientRequest {

    @NotBlank(message = "Nom de société est requis")
    private String nomSociete;

    @NotBlank(message = "Siège social est requis")
    private String siegeSocial;


    private String adresse;

    @NotBlank(message = "Nom du directeur est requis")
    private String nomDirecteur;

    @NotBlank(message = "Prénom du directeur est requis")
    private String prenomDirecteur;


    @Pattern(
            regexp = "^(\\+216[2459]\\d{7})|(\\+33[1-9]\\d{8})$",
            message = "Le numéro doit être un numéro valide en +216 ou +33"
    )
    private String telephoneDirecteur;
}
