package com.example.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ContabilizacionRequestDto;
import com.example.backend.dto.ContabilizacionResponseDto;
import com.example.backend.service.ContabilizacionService;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Fdt;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.FdtRepository;
import com.example.backend.sqlserver2.repository.TerRepository;

@RestController
@RequestMapping("/api/contabilizacion")
public class ContabilizacionController {

    @Autowired
    private ContabilizacionService contabilizacionService;

    @Autowired
    private FacRepository facRepository;

    @Autowired
    private FdeRepository fdeRepository;

    @Autowired
    private FdtRepository fdtRepository;

    @Autowired
    private TerRepository terRepository;

    @PostMapping("/generar")
    public ResponseEntity<?> generarOperacion(@RequestBody ContabilizacionRequestDto request) {
        try {
            if (request.getEnt() == null || request.getEntcod() == null || request.getEje() == null || request.getFacnum() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios: ent, entcod, eje, facnum");
            }

            if (request.getFechaContable() == null || request.getFechaContable().isBlank()) {
                ContabilizacionResponseDto error = new ContabilizacionResponseDto();
                error.setExito(false);
                error.setMensaje("Falta fecha contable");
                return ResponseEntity.badRequest().body(error);
            }

            String fcNormalizada = request.getFechaContable().replace("-", "");
            if (fcNormalizada.length() < 4) {
                ContabilizacionResponseDto error = new ContabilizacionResponseDto();
                error.setExito(false);
                error.setMensaje("Falta fecha contable");
                return ResponseEntity.badRequest().body(error);
            }
            String anioFecha = fcNormalizada.substring(0, 4);
            if (!anioFecha.equals(request.getEje())) {
                ContabilizacionResponseDto error = new ContabilizacionResponseDto();
                error.setExito(false);
                error.setMensaje("La fecha contable debe pertenecer al ejercicio contable");
                return ResponseEntity.badRequest().body(error);
            }

            FacId facId = new FacId(request.getEntcod(), request.getEje(), request.getFacnum());
            Optional<Fac> facOpt = facRepository.findById(facId);

            if (facOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Factura no encontrada");
            }

            Fac fac = facOpt.get();

            String terAyt = null;
            if (fac.getTERCOD() != null) {
                Optional<Ter> terOpt = terRepository.findByENTAndTERCOD(request.getEntcod(), fac.getTERCOD());
                if (terOpt.isPresent() && terOpt.get().getTERAYT() != null) {
                    terAyt = String.valueOf(terOpt.get().getTERAYT());
                }
            }

            List<Fde> fdeList = fdeRepository.findByENTAndEJEAndFACNUM(request.getEntcod(), request.getEje(), request.getFacnum());
            List<Fdt> fdtList = fdtRepository.findByENTAndEJEAndFACNUM(request.getEntcod(), request.getEje(), request.getFacnum());

            String smlInput = contabilizacionService.buildSmlInput(request, fac, fdeList, fdtList, terAyt);
            String soapResponse = contabilizacionService.sendSmlRequest(smlInput, request.getWebserviceUrl());
            ContabilizacionResponseDto response = contabilizacionService.parseResponse(soapResponse);

            if (response.isExito()) {
                if (response.getOpesical() != null) {
                    fac.setFACADO(response.getOpesical());
                }
                if (fcNormalizada.length() == 8) {
                    int year = Integer.parseInt(fcNormalizada.substring(0, 4));
                    int month = Integer.parseInt(fcNormalizada.substring(4, 6));
                    int day = Integer.parseInt(fcNormalizada.substring(6, 8));
                    fac.setFACFCO(LocalDateTime.of(year, month, day, 0, 0));
                }
                facRepository.save(fac);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception ex) {
            ContabilizacionResponseDto error = new ContabilizacionResponseDto();
            error.setExito(false);
            error.setMensaje("Error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}