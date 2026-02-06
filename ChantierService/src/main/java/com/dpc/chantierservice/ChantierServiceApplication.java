package com.dpc.chantierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.dpc.chantierservice.feign")
public class ChantierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChantierServiceApplication.class, args);
    }

}
