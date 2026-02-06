package com.dpc.chantierservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String telephone;
    private String adresse;
    private String type;
    private String rib;
    private String soustraitant;
    private String nomRepresentant;
    private String prenomRepresentant;
    private boolean archived;
}
