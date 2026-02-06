package com.dpc.chantierservice.feign;

import com.dpc.chantierservice.dto.EmployeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@FeignClient(name = "EmployeService", url = "https://beeconstruct-employe.dpc.com.tn")
@FeignClient(name = "EmployeService", url = "http://localhost:8093")
public interface EmployeFeignClient {


    @GetMapping("/employes/{id}")
    EmployeDTO getEmployeById(@PathVariable("id") Long id,@RequestHeader("Authorization") String token);

    @GetMapping("/employes/by-ids")
    List<EmployeDTO> getEmployesByIds(@RequestParam("ids") List<Long> ids,@RequestHeader("Authorization") String token);
}
