package com.example.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.COGAIPOnlyDto;
import com.example.backend.dto.CogCgeProjection;
import com.example.backend.dto.CogSaveDto;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.CogId;
import com.example.backend.sqlserver2.repository.CogRepository;

@RestController
@RequestMapping("/api/cog")
public class CogController {
    @Autowired
    private CogRepository cogRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //selecting centro gestores for contrato
    @GetMapping("/fetch-centros/{ent}/{eje}/{concod}")
    public ResponseEntity<?> fetchCentroGestores(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod
    ) {
        try {
            List<CogCgeProjection> centros = cogRepository.findAllByENTAndEJEAndCONCOD(ent, eje, concod);
            if (centros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(centros);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting a centro gestor from contrato
    @DeleteMapping("/delete-centro/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> deleteCentroGestore(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod
    ) {
        try {
            Optional<COGAIPOnlyDto> centro =  cogRepository.findByENTAndEJEAndCONCODAndCGECOD(ent, eje, concod, cgecod);

            if (centro.isPresent()) {
                Double cogiap = centro.get().getCOGIAP();
                if (cogiap != null && cogiap > 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede quitar un centro gestor donde ya hay pedidos");
                } else if (cogiap != null && cogiap == 0) {
                    CogId id = new CogId(ent, eje, concod, cgecod);
                    cogRepository.deleteById(id);

                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //saving centro gestores to a contrato
    @PostMapping("/save-centroGestores")
    public ResponseEntity<?> saveCentros(
        @RequestBody List<CogSaveDto> items
    ) {
        try {
            List<Cog> toSave = new ArrayList<>();
            for (CogSaveDto dto: items) {
                boolean exists = cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(dto.ent, dto.eje, dto.concod, dto.cgecod);
                if (!exists) {
                    Cog c = new Cog();
                    c.setENT(dto.ent);
                    c.setEJE(dto.eje);
                    c.setCONCOD(dto.concod);
                    c.setCGECOD(dto.cgecod);
                    c.setCOGIMP(dto.cogimp);
                    c.setCOGIAP(dto.cogiap);
                    toSave.add(c);
                }
            }
            cogRepository.saveAll(toSave);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding first D to a contrato's centro gestor
    public record AddD(Double COGIMP, String COGOPD) {}

    @PatchMapping("/update-centro-D/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> addDCentro(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod,
        @RequestBody AddD payload
    ) {
        try {
            if (payload == null || payload.COGIMP() == null || payload.COGOPD() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> centro = cogRepository.findById(id);
            if (centro.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Cog updateCentro = centro.get();
            updateCentro.setCOGIMP(payload.COGIMP());
            updateCentro.setCOGOPD(payload.COGOPD());
            cogRepository.save(updateCentro);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding second D to a contrato's centro gestor
    public record AddD2(Double COGIM2, String COGOP2, String COGRF2) {}

    @PatchMapping("/update-centro-D2/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> addDCentro(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod,
        @RequestBody AddD2 payload
    ) {
        try {
            if (payload == null || payload.COGIM2() == null || payload.COGOP2() == null || payload.COGRF2() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> centro = cogRepository.findById(id);
            if (centro.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Cog updateCentro = centro.get();
            updateCentro.setCOGIM2(payload.COGIM2());
            updateCentro.setCOGOP2(payload.COGOP2());
            updateCentro.setCOGRF2(payload.COGRF2());
            cogRepository.save(updateCentro);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting first D
    @DeleteMapping("/delete-D/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> deleteD(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod
    ) {
        try {
            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> cog = cogRepository.findById(id);
            if (cog.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Cog DToDelete = cog.get();
            DToDelete.setCOGIMP(0.00);
            DToDelete.setCOGOPD("");
            DToDelete.setCOGRFD("");
            cogRepository.save(DToDelete);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting second D
    @DeleteMapping("/delete-D2/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> deleteD2(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod
    ) {
        try {
            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> cog = cogRepository.findById(id);
            if (cog.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Cog DToDelete = cog.get();
            DToDelete.setCOGIM2(0.00);
            DToDelete.setCOGOP2("");
            DToDelete.setCOGRF2("");
            cogRepository.save(DToDelete);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update cogimp
    public record UpdateImp(Double COGIMP) {}
    @PatchMapping("/updateD1/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> updateD1(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod,
        @RequestBody UpdateImp payload
    ) {
        try {
            if (payload == null || payload.COGIMP() == null) {
                return ResponseEntity.badRequest().body("COGIMP is required.");
            }

            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> result = cogRepository.findById(id);

            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Cog cog = result.get();
            cog.setCOGIMP(payload.COGIMP());
            cogRepository.save(cog);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update cogim2
    public record UpdateIm2(Double COGIM2) {}
    @PatchMapping("/updateD2/{ent}/{eje}/{concod}/{cgecod}")
    public ResponseEntity<?> updateD2(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @PathVariable String cgecod,
        @RequestBody UpdateIm2 payload
    ) {
        try {
            if (payload == null || payload.COGIM2() == null) {
                return ResponseEntity.badRequest().body("COGIM2 is required.");
            }

            CogId id = new CogId(ent, eje, concod, cgecod);
            Optional<Cog> result = cogRepository.findById(id);

            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Cog cog = result.get();
            cog.setCOGIM2(payload.COGIM2());
            cogRepository.save(cog);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
