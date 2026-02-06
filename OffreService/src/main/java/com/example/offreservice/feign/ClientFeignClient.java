package com.example.offreservice.feign;

import com.example.offreservice.dto.ClientDTO;

import feign.Client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


//@FeignClient(name = "ClientsService", url = "https://beeconstruct-client.dpc.com.tn")
@FeignClient(name = "ClientsService", url = "http://localhost:8092")



public interface ClientFeignClient {
    @GetMapping("/clients/{id}")
    ClientDTO getClientById(@PathVariable("id") Long id, @RequestHeader("Authorization") String authorizationHeader);
    @GetMapping("/clients/active")
    List<ClientDTO> getAllActiveClients(
            @RequestHeader("Authorization") String authorizationHeader
    );





}
