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
public class addEmployeRequest {

    private String firstName;


    private String lastName;



    @Pattern(
            regexp = "^(\\+216[2459]\\d{7})|(\\+33[1-9]\\d{8})$",
            message = "Phone number must be a valid Tunisian (+216) or French (+33) number."
    )
    private String telephone;

    @NotBlank(message = "Passport is required")
    private String adresse;



    private String rib;








    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }



    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }
}
