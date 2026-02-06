package com.example.offreservice.feign;



import com.example.offreservice.dto.ChantierDTO;
import com.example.offreservice.dto.request.CreateChantierRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ChantierService", url = "http://localhost:8097")
//@FeignClient(name = "ChantierService", url = "https://beeconstruct-chantier.dpc.com.tn")
public interface ChantierFeignClient {
    @PostMapping("chantiers")
    ChantierDTO createChantier(@RequestBody CreateChantierRequest request, @RequestHeader("Authorization") String token);

    @GetMapping("chantiers/by-offre/{offreId}")
    ChantierDTO getChantierByOffreId(@PathVariable("offreId") Long offreId, @RequestHeader("Authorization") String token);
    @DeleteMapping("chantiers/{id}")
    void deleteChantier(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
}
