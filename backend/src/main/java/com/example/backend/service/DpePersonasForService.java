package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dto.ServicePersonaRequest;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.model.DpeId;
import com.example.backend.sqlserver2.repository.DpeRepository;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DpePersonasForService {
    private final DpeRepository dpeRepository;

    public DpePersonasForService(DpeRepository dpeRepository) {
        this.dpeRepository = dpeRepository;
    }

    @Transactional
    public NamesResponse saveServicePersonas(ServicePersonaRequest req) {

        List<String> savedNames = new ArrayList<>();
        List<String> unsavedNames = new ArrayList<>();

        if (req.getPersonas() == null || req.getPersonas().isEmpty()) {
            return new NamesResponse(savedNames, unsavedNames);
        }

        Integer ent = req.getEnt();
        String eje = req.getEje();
        String depcod = req.getDepcod();

        for (String percod : req.getPersonas()) {
            DpeId id = new DpeId(ent, eje, depcod, percod);
            Optional<Dpe> persona = dpeRepository.findById(id);
            if (persona.isPresent()) {
                unsavedNames.add(persona.get().getPERCOD());
            } else {
                Dpe dpe = new Dpe();
                dpe.setENT(ent);
                dpe.setEJE(eje);
                dpe.setPERCOD(percod);
                dpe.setDEPCOD(depcod);
                dpeRepository.save(dpe);

                savedNames.add(percod);
            }
        }
        return new NamesResponse(savedNames, unsavedNames);
    }

    public record NamesResponse (
        List<String> savedNames,
        List<String> unsavedNames
    ) {}
}