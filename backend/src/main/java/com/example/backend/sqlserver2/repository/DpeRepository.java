package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import com.example.backend.dto.personasPorServiciosProjection;
import com.example.backend.dto.ServiceMagsProjection;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.model.DpeId;

@Repository
public interface DpeRepository extends JpaRepository<Dpe, DpeId> {
    //fetch for C.exitencias almacen
    List<ServiceMagsProjection> findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(Integer ent, String eje, String percod, String cgecod, Integer depalm);

    //inserting services
    List<Dpe> findByENTAndEJEAndDEPCOD(Integer ENT, String EJE, String DEPCOD);

    //needed for copy perfil function and selecting centro getor for login and selecting a persona's services
    List<Dpe> findByENTAndEJEAndPERCOD(Integer ENT, String EJE, String PERCOD);

    //deleting a persona
    @Transactional
    @Modifying
    int deleteByENTAndEJEAndPERCOD(Integer ENT, String EJE, String PERCOD);

    //selecting personas por servicios
    List<personasPorServiciosProjection> findByENTAndEJE(Integer ENT, String EJE, Pageable pageable);

    //getting the number of pages
    Integer countByENTAndEJE(Integer ent, String eje);

    //downloading as excel
    List<personasPorServiciosProjection> findByENTAndEJE(Integer ENT, String EJE);
    
    // Persona
    List<personasPorServiciosProjection> findProjectionByENTAndEJEAndPERCOD(Integer ent, String eje, String percod);
    List<personasPorServiciosProjection> findByENTAndEJEAndPer_PERNOMContaining(Integer ent, String eje, String pernom);
    
    // Centro Gestor
    List<personasPorServiciosProjection> findByENTAndEJEAndDep_Cge_CGECOD(Integer ent, String eje, String cgecod);
    
    // Persona + CGE
    List<personasPorServiciosProjection> findByENTAndEJEAndPer_PERNOMContainingAndDep_Cge_CGECOD(Integer ent, String eje, String pernom, String cgecod);
    List<personasPorServiciosProjection> findByENTAndEJEAndPERCODAndDep_Cge_CGECOD(Integer ent, String eje, String percod, String cgecod);
}