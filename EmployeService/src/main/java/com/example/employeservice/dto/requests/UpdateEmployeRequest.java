package com.example.employeservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateEmployeRequest {


    private String firstName;


    private String lastName;


    @Pattern(
            regexp = "^(\\+216[2459]\\d{7})|(\\+33[1-9]\\d{8})$",
            message = "Phone number must be a valid Tunisian (+216) or French (+33) number."
    )
    private String telephone;

    @NotBlank(message = "Adresse is required")
    private String adresse;



    private String rib;



}
