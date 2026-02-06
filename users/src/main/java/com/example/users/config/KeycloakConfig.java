package com.example.users.config;


import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Bean
    public Keycloak keycloak(){

        return KeycloakBuilder.builder()
                .clientSecret("YMzciivjvgqwyVLavpYKcWbjL5QHwgRr")
                .clientId("BeeConstructBack")
                .grantType("client_credentials")
                .realm("BeeConstruct")
                .serverUrl("https://keycloak.dpc.com.tn:8443")
                .build();
    }
}

