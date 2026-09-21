package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dto.PersonaServiceRequest;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.model.DpeId;
import com.example.backend.sqlserver2.repository.DpeRepository;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DpeService {

    private final DpeRepository dpeRepository;

    public DpeService(DpeRepository dpeRepository) {
        this.dpeRepository = dpeRepository;
    }

    @Transactional
    public NamesResponse savePersonaServices(PersonaServiceRequest req) {

        List<String> savedNames = new ArrayList<>();
        List<String> unsavedNames = new ArrayList<>();

        if (req.getServices() == null || req.getServices().isEmpty()) {
            return new NamesResponse(savedNames, unsavedNames);
        }

        Integer ent = req.getEnt();
        String eje = req.getEje();
        String percod = req.getPercod();

        for (String depcod : req.getServices()) {
            DpeId id = new DpeId(ent, eje, depcod, percod);
            Optional<Dpe> servicio = dpeRepository.findById(id);
            if (servicio.isPresent()) {
                unsavedNames.add(servicio.get().getDEPCOD());
            } else {
                Dpe dpe = new Dpe();
                dpe.setENT(ent);
                dpe.setEJE(eje);
                dpe.setDEPCOD(depcod);
                dpe.setPERCOD(percod);
                dpeRepository.save(dpe);

                savedNames.add(depcod);
            }
        }
        return new NamesResponse(savedNames, unsavedNames);
    }

    public record NamesResponse (
        List<String> savedNames,
        List<String> unsavedNames
    ) {}
}