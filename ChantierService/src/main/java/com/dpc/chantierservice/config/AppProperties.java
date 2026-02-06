package com.dpc.chantierservice.config;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @NotBlank
    private String minioUrl;
    @NotBlank
    private String minioBucket;

    @NotBlank
    private String minioAccessKey;

    @NotBlank
    private String minioSecretKey;

    @NotBlank
    private String keycloakAdminClientId;

    @NotBlank
    private String keycloakAdminClientSecret;

    @NotBlank
    private String keycloakRealm;

    @NotBlank
    private String keycloakServerurl;
}
