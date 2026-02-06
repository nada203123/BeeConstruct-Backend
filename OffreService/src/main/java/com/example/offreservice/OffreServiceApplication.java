package com.example.offreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;



@SpringBootApplication
@EnableFeignClients
public class OffreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OffreServiceApplication.class, args);
    }

}
