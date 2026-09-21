package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.DepWithCgeView;
import com.example.backend.sqlserver2.model.Dep;
import com.example.backend.sqlserver2.model.DepId;
import com.example.backend.sqlserver2.repository.DepRepository;
import com.example.backend.sqlserver2.repository.CcoRepository;

@RestController
@RequestMapping("/api/dep")
public class DepController {
    @Autowired
    private DepRepository depRepository;
    @Autowired
    private CcoRepository ccoRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";
    private static final String FALTAN = "Faltan datos obligatorios";

    //fetching all services
    @GetMapping("/fetch-services/{ent}/{eje}")
    public ResponseEntity<?> fetchServices(
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<Dep> services = depRepository.findByENTAndEJE(ent, eje);
            if (services.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(services);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //fetching services for a user (main panel)
    @GetMapping("/fetch-services-persona/{ent}/{eje}/{percod}")
    public ResponseEntity<?> fetchServicesPersona(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String percod
    ) {
        try {
            List<DepWithCgeView> services = depRepository.findByENTAndEJEAndDpes_PERCOD(ent, eje, percod);
            if (services.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(services);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //modifying a service
    public record ServiceUpdate(String depdes, Integer depalm, Integer depcom, Integer depint) {}

    @PatchMapping("/update-service/{ent}/{eje}/{depcod}")
    @Transactional
    public ResponseEntity<?> updateCentro(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String depcod,
        @RequestBody ServiceUpdate payload
    ) {
        try {
            if (payload == null || payload.depdes() == null || payload.depalm() == null || payload.depcom() == null || payload.depint() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }
 
            DepId id = new DepId(ent, eje, depcod);
            Optional<Dep> service = depRepository.findById(id);

            if (service.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Dep d = service.get();
            d.setDEPDES(payload.depdes());
            d.setDEPALM(payload.depalm());
            d.setDEPCOM(payload.depcom());
            d.setDEPINT(payload.depint());

            depRepository.save(d);
            return ResponseEntity.noContent().build();

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //modifying rest of service 
    public record ServiceUpdateSecond(
    String depd1c, String depd1d,
    String depd2c, String depd2d,
    String depd3c, String depd3d,
    String depdco, String depden) {}

    @PatchMapping("/update-service-second/{ent}/{eje}/{depcod}")
    @Transactional
    public ResponseEntity<?> updateServiceSecond(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String depcod,
        @RequestBody ServiceUpdateSecond payload
    ) {
        try {
            if (payload == null ||
                payload.depd1c() == null || payload.depd1d() == null ||
                payload.depd2c() == null || payload.depd2d() == null ||
                payload.depd3c() == null || payload.depd3d() == null ||
                payload.depdco() == null || payload.depden() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }

            DepId id = new DepId(ent, eje, depcod);
            Optional<Dep> service = depRepository.findById(id);

            if (service.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Dep d = service.get();
            d.setDEPD1C(payload.depd1c());
            d.setDEPD1D(payload.depd1d());
            d.setDEPD2C(payload.depd2c());
            d.setDEPD2D(payload.depd2d());
            d.setDEPD3C(payload.depd3c());
            d.setDEPD3D(payload.depd3d());
            d.setDEPDCO(payload.depdco());
            d.setDEPDEN(payload.depden());

            depRepository.save(d);
            return ResponseEntity.noContent().build();

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding a service
    public record ServiceAdd(Integer ent, String eje, String depcod, String depdes, Integer depalm, Integer depcom, Integer depint, String ccocod, String cgecod, String depd1c, String depd1d, String depd2c, String depd2d, String depd3c, String depd3d, String depdco, String depden, String percod) {}
    @PostMapping("/Insert-service")
    public ResponseEntity<?> addCentroGestor(
        @RequestBody ServiceAdd payload
    ) {
        try {
            if (payload == null || payload.ent() == null || payload.eje() == null || payload.depcod() == null || payload.depdes() == null || payload.depalm() == null || payload.depcom() == null || payload.depint() == null || payload.ccocod() == null || payload.cgecod() == null || payload.percod() == null) {
                return ResponseEntity.badRequest().body(FALTAN);
            }
            if(!depRepository.findByENTAndEJEAndDEPCOD(payload.ent(), payload.eje(), payload.depcod()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Este servicio ya existe");
            }
            if(ccoRepository.countByENTAndEJEAndCCOCOD(payload.ent(), payload.eje(), payload.ccocod()) == 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El Centro de Coste no existe");
            }

            Dep nueva = new Dep();
            nueva.setENT(payload.ent());
            nueva.setEJE(payload.eje());
            nueva.setDEPCOD(payload.depcod());
            nueva.setDEPDES(payload.depdes());
            nueva.setDEPALM(payload.depalm());
            nueva.setDEPCOM(payload.depcom());
            nueva.setDEPINT(payload.depint());
            nueva.setCCOCOD(payload.ccocod());
            nueva.setCGECOD(payload.cgecod());
            nueva.setDEPD1C(payload.depd1c());
            nueva.setDEPD1D(payload.depd1d());
            nueva.setDEPD2C(payload.depd2c());
            nueva.setDEPD2D(payload.depd2d());
            nueva.setDEPD3C(payload.depd3c());
            nueva.setDEPD3D(payload.depd3d());
            nueva.setDEPDCO(payload.depdco());
            nueva.setDEPDEN(payload.depden());
            depRepository.save(nueva);
            
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search function
    @GetMapping("/search")
    public ResponseEntity<?> searchServices(
        @RequestParam Integer ent,
        @RequestParam String eje,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String cgecod,
        @RequestParam(required = false) String perfil
    ) {
        try {
            List<Dep> base = depRepository.findByENTAndEJE(ent, eje);
            base = filterBySearch(base, search);
            base = filterByCgecod(base, cgecod);
            base = filterByPerfil(base, perfil);

            if (base.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(base);

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    private List<Dep> filterBySearch(List<Dep> services, String search) {
        if (search == null || search.isBlank()) {
            return services;
        }
        final String searchLower = search.toLowerCase();
        return services.stream()
            .filter(d -> {
                String depcod = nullSafeString(d.getDEPCOD()).toLowerCase();
                String depdes = nullSafeString(d.getDEPDES()).toLowerCase();
                return depcod.contains(searchLower) || depdes.contains(searchLower);
            })
            .toList();
    }

    private String nullSafeString(String value) {
        return value == null ? "" : value;
    }
    private List<Dep> filterByCgecod(List<Dep> services, String cgecod) {
        if (cgecod == null || cgecod.isBlank()) {
            return services;
        }
        return services.stream()
            .filter(d -> cgecod.equalsIgnoreCase(d.getCGECOD()))
            .toList();
    }

    private List<Dep> filterByPerfil(List<Dep> services, String perfil) {
        if (perfil == null || perfil.isBlank() || perfil.equalsIgnoreCase("todos")) {
            return services;
        }
        return services.stream()
            .filter(d -> matchesPerfil(d, perfil))
            .toList();
    }

    private boolean matchesPerfil(Dep dep, String perfil) {
        return switch (perfil.toLowerCase()) {
            case "almacen" -> isAlmacenUser(dep);
            case "comprador" -> isCompradorUser(dep);
            case "contabilidad" -> isContabilidadUser(dep);
            case "peticionario" -> isPeticionarioUser(dep);
            default -> true;
        };
    }

    private boolean isAlmacenUser(Dep dep) {
        return dep.getDEPALM() != null && dep.getDEPALM() == 1;
    }

    private boolean isCompradorUser(Dep dep) {
        return dep.getDEPCOM() != null && dep.getDEPCOM() == 1;
    }

    private boolean isContabilidadUser(Dep dep) {
        return dep.getDEPINT() != null && dep.getDEPINT() == 1;
    }

    private boolean isPeticionarioUser(Dep dep) {
        return (dep.getDEPALM() == null || dep.getDEPALM() == 0) &&
            (dep.getDEPCOM() == null || dep.getDEPCOM() == 0) &&
            (dep.getDEPINT() == null || dep.getDEPINT() == 0);
    }
}