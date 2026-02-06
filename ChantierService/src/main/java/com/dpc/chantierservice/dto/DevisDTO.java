package com.dpc.chantierservice.dto;

import lombok.Data;

@Data
public class DevisDTO {
    private Long id;
    private Long offerId;
    private String offer;
    private Long clientId;
    private String clientFirstName;
    private String clientLastName;

}
