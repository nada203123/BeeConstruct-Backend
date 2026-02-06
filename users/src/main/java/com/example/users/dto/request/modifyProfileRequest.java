package com.example.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public class modifyProfileRequest {
    //@NotBlank(message = "First name is required")
    private String firstName;

   // @NotBlank(message = "Last name is required")
    private String lastName;


   // @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+216[2459]\\d{7})|(\\+33[1-9]\\d{8})$",
            message = "Phone number must be a valid Tunisian (+216) or French (+33) number."
    )
    private String phoneNumber;

    @Schema(type = "string", format = "binary", description = "Image de profil")
    private MultipartFile image;

    private String country;

    private String gender;



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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


}
