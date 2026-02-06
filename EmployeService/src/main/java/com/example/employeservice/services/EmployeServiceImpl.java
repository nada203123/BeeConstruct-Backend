package com.example.employeservice.services;

import com.example.employeservice.dto.requests.UpdateEmployeRequest;
import com.example.employeservice.dto.requests.addEmployeRequest;
import com.example.employeservice.model.Employe;
import com.example.employeservice.repositories.EmployeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;
import java.util.List;

@Service
@Slf4j
public class EmployeServiceImpl implements EmployeService{
    private final EmployeRepository employeRepository;

    public EmployeServiceImpl(EmployeRepository employeRepository ) {
        this.employeRepository = employeRepository;
    }

    @Override
    public Employe addEmploye(addEmployeRequest request) {
        // Check telephone uniqueness only for PER_DIEM and FORFAIT
        if ( employeRepository.existsByTelephone(request.getTelephone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le numéro du téléphone existe déjà.");
        }



        // Skip rib uniqueness check for sous-traitants
        if (employeRepository.existsByRib(request.getRib())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le RIB existe déjà.");
        }


        return employeRepository.save(Employe.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .rib(request.getRib())
                .archived(false)
                .build());
    }


    @Override
    public List<Employe> getAllActiveEmployes() {
        return employeRepository.findByArchivedFalse();
    }

    @Override
    public List<Employe> getAllArchivedEmployes() {
        return employeRepository.findByArchivedTrue();
    }

    @Override
    public Employe archiveEmploye(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));

        if (employe.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employe is already archived");
        }

        employe.setArchived(true);
        return employeRepository.save(employe);
    }

    @Override
    public Employe restoreEmploye(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));

        if (!employe.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employe is not archived");
        }

        employe.setArchived(false);
        return employeRepository.save(employe);
    }

    @Override
    public Employe updateEmploye(Long id, UpdateEmployeRequest request) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));

        // Check telephone uniqueness only for PER_DIEM and FORFAIT
        if (!Objects.equals(employe.getTelephone(), request.getTelephone())) {
            if (request.getTelephone() != null && employeRepository.existsByTelephone(request.getTelephone())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le numéro de téléphone existe déjà.");
            }
        }



        // Skip rib uniqueness check for sous-traitant employees

            if (!Objects.equals(employe.getRib(), request.getRib())) {
                if (request.getRib() != null && employeRepository.existsByRib(request.getRib())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Le rib existe déjà.");
                }
            }


        // Update the fields
        employe.setFirstName(request.getFirstName());
        employe.setLastName(request.getLastName());
        employe.setTelephone(request.getTelephone());
        employe.setAdresse(request.getAdresse());
        employe.setRib(request.getRib());


        return employeRepository.save(employe);
    }


    @Override
    public void deleteEmploye(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
        employeRepository.delete(employe);
    }





    @Override
    public Employe getEmployeById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
    }


    @Override
    public List<Employe> getEmployesByIds(List<Long> ids) {
        List<Employe> employes = employeRepository.findAllById(ids);

        // Optional: ensure all IDs were found, throw if any are missing
        if (employes.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more employes not found");
        }

        return employes;
    }



}
