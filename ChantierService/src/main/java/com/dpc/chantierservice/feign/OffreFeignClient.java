package com.dpc.chantierservice.feign;

import com.dpc.chantierservice.dto.OffreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
//@FeignClient(name = "OffreService", url = "https://beeconstruct-offre.dpc.com.tn")
@FeignClient(name = "OffreService", url = "http://localhost:8095")
public interface OffreFeignClient {
    @GetMapping("/offres/{id}")
    OffreDTO getOffreById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/offres/active")
    List<OffreDTO> getAllActiveOffers(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/offres/{id}/client")
    OffreDTO getAllActiveOfferswithClient(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    );
}
