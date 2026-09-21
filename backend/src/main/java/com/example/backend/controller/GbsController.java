package com.example.backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.example.backend.dto.GbsWithCgeDto;
import com.example.backend.dto.bolsaSaveDto;
import com.example.backend.sqlserver2.model.Cge;
import com.example.backend.sqlserver2.model.CgeId;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.model.GbsId;
import com.example.backend.sqlserver2.repository.CgeRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;

@RestController
@RequestMapping("/api/gbs")
public class GbsController {
    @Autowired
    private GbsRepository gbsRepository;
    @Autowired
    private CgeRepository cgeRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";
    private static final String FALTAN = "Faltan datos obligatorios";

    //for the main list of bolsa por cge
    @GetMapping("fetch-all/{ent}/{eje}/{cgecod}")
    public ResponseEntity<?> getBolsas(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<Gbs> gbsList = gbsRepository.findByENTAndEJEAndCGECOD(ent, eje, cgecod);
            
            Optional<Cge> cgeOpt = cgeRepository.findById(new CgeId(ent, eje, cgecod));
            if (cgeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            Cge cge = cgeOpt.get();

            List<GbsWithCgeDto> result = gbsList.stream().map(g -> new GbsWithCgeDto.Builder()
                .cgecod(cge.getCGECOD())
                .cgedes(cge.getCGEDES())
                .cgecic(cge.getCGECIC() != null ? String.valueOf(cge.getCGECIC()) : null)
                .gbsref(g.getGBSREF())
                .gbsope(g.getGBSOPE())
                .gbsorg(g.getGBSORG())
                .gbsfun(g.getGBSFUN())
                .gbseco(g.getGBSECO())
                .gbsfop(g.getGBSFOP())
                .gbsimp(g.getGBSIMP())
                .gbsibg(g.getGBSIBG())
                .gbsius(g.getGBSIUS())
                .gbsico(g.getGBSICO())
                .gbsiut(g.getGBSIUT())
                .gbsict(g.getGBSICT())
                .gbs413(g.getGBS413())
                .build()
            ).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //modifying a bolsa
    public record updateBolsa(Double GBSIMP, Double GBSIUS, Double GBSICO, LocalDateTime GBSFOP, Double GBSIBG) {}
    @PatchMapping("updateBolsa/{ent}/{eje}/{cgecod}/{gbsref}")
    public ResponseEntity<?> updateBolsa(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String cgecod,
            @PathVariable String gbsref,
            @RequestBody updateBolsa payload
    ) {
        try {
            if (payload == null || payload.GBSIMP() == null || payload.GBSIUS() == null || payload.GBSICO() == null || payload.GBSFOP() == null || payload.GBSIBG() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }

            GbsId id = new GbsId(ent, eje, cgecod, gbsref);
            Optional<Gbs> bolsa = gbsRepository.findById(id);
            if (bolsa.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Gbs bolsaUpdate = bolsa.get();
            bolsaUpdate.setGBSIMP(payload.GBSIMP());
            bolsaUpdate.setGBSIUS(payload.GBSIUS());
            bolsaUpdate.setGBSICO(payload.GBSICO());
            bolsaUpdate.setGBSFOP(payload.GBSFOP());
            bolsaUpdate.setGBSIBG(payload.GBSIBG());
            gbsRepository.save(bolsaUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //modifying gbsibg only
    public record updateGbsimp(Double GBSIMP, Double GBSIUS, Double GBSICO, LocalDateTime GBSFOP) {}
    @PatchMapping("update-Importe/{ent}/{eje}/{cgecod}/{gbsref}")
    public ResponseEntity<?> updateGbsimp(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod,
        @PathVariable String gbsref,
        @RequestBody updateGbsimp payload
    ) {
        try {
            if (payload == null || payload.GBSIMP() == null || payload.GBSIUS() == null || payload.GBSICO() == null || payload.GBSFOP() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }

            GbsId id = new GbsId(ent, eje, cgecod, gbsref);
            Optional<Gbs> bolsa = gbsRepository.findById(id);
            if (bolsa.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Gbs bolsaUpdate = bolsa.get();
            bolsaUpdate.setGBSIMP(payload.GBSIMP());
            bolsaUpdate.setGBSIUS(payload.GBSIUS());
            bolsaUpdate.setGBSICO(payload.GBSICO());
            bolsaUpdate.setGBSFOP(payload.GBSFOP());
            gbsRepository.save(bolsaUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //Traspasar bolsa de gestión
    public record trans(Double GBSIMP, Double GBSIBG, Double GBSIUS, Double GBSICO, LocalDateTime GBSFOP) {}
    @PatchMapping("transpasar-bolsa/{ent}/{eje}/{cgecod}/{gbsref}")
    public ResponseEntity<?> TranspasarBolsa(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String cgecod,
            @PathVariable String gbsref,
            @RequestBody trans payload
    ) {
        try {
            if (payload == null || payload.GBSIMP() == null || payload.GBSIUS() == null || payload.GBSICO() == null || payload.GBSFOP() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }

            GbsId id = new GbsId(ent, eje, cgecod, gbsref);
            Optional<Gbs> bolsa = gbsRepository.findById(id);
            if (bolsa.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Gbs bolsaUpdate = bolsa.get();
            bolsaUpdate.setGBSIMP(payload.GBSIMP());
            bolsaUpdate.setGBSIBG(payload.GBSIBG());
            bolsaUpdate.setGBSIUS(payload.GBSIUS());
            bolsaUpdate.setGBSICO(payload.GBSICO());
            bolsaUpdate.setGBSFOP(payload.GBSFOP());

            gbsRepository.save(bolsaUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding a bolsa
    @PostMapping("/add-Bolsa")
    public ResponseEntity<?> addBolsa(
        @RequestBody List<bolsaSaveDto> items
    ) {
        try {
            List<String> savedBolsas = new ArrayList<>();
            List<String> unSavedBolsas = new ArrayList<>();

            List<Gbs> toSave = new ArrayList<>();
            for (bolsaSaveDto dto: items) {
                Optional<Gbs> bolsa = gbsRepository.findByENTAndEJEAndCGECODAndGBSREFAndGBSORGAndGBSFUNAndGBSECO(dto.ENT, dto.EJE, dto.CGECOD, dto.GBSREF, dto.GBSORG, dto.GBSFUN, dto.GBSECO);

                if (!bolsa.isEmpty()) {
                    unSavedBolsas.add(bolsa.get().getGBSREF());
                } else {
                    Gbs addBolsa = new Gbs();
                    addBolsa.setENT(dto.ENT);
                    addBolsa.setEJE(dto.EJE);
                    addBolsa.setCGECOD(dto.CGECOD);
                    addBolsa.setGBSREF(dto.GBSREF);
                    addBolsa.setGBSOPE(dto.GBSOPE);
                    addBolsa.setGBSORG(dto.GBSORG);
                    addBolsa.setGBSFUN(dto.GBSFUN);
                    addBolsa.setGBSECO(dto.GBSECO);
                    addBolsa.setGBSIMP(0.00);
                    addBolsa.setGBSIBG(0.00);
                    addBolsa.setGBSIUS(0.00);
                    addBolsa.setGBSICO(0.00);
                    addBolsa.setGBSIUT(0.00);
                    addBolsa.setGBSICT(0.00);
                    addBolsa.setGBS413(0.00);
                    toSave.add(addBolsa);

                    savedBolsas.add(dto.GBSREF);
                }
            }
            gbsRepository.saveAll(toSave);

            return ResponseEntity.status(HttpStatus.CREATED).body(new NamesResponse(savedBolsas, unSavedBolsas));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    public record NamesResponse (
        List<String> savedBolsas,
        List<String> unSavedBolsas
    ) {}

    //deleting a bolsa
    @DeleteMapping("/delete-bolsa/{ent}/{eje}/{cgecod}/{gbsref}")
    public ResponseEntity<?> deleteBolsa(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod,
        @PathVariable String gbsref
    ) {
        try {
            GbsId id = new GbsId(ent, eje, cgecod, gbsref);
            Optional<Gbs> bolsa = gbsRepository.findById(id);
            if (bolsa.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            } else {
                Double gbsiut = bolsa.get().getGBSIUT();
                if (Double.compare(gbsiut, 0.0) != 0) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede eliminar la aplicación. Está en uso");
                } else {
                    gbsRepository.deleteById(id);
                }
            }

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}