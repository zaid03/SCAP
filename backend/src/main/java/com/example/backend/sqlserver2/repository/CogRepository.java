package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.CogId;
import com.example.backend.dto.COGAIPOnlyDto;
import com.example.backend.dto.CogCgeProjection;

import java.util.List;
import java.util.Optional;

public interface CogRepository extends JpaRepository<Cog, CogId> {
    //selecting centro gestores for contrato
    List<CogCgeProjection> findAllByENTAndEJEAndCONCOD(Integer ENT, String EJE, Integer CONCOD);

    //needed for deleting a centro gestor from a contrato
    Optional<COGAIPOnlyDto> findByENTAndEJEAndCONCODAndCGECOD(Integer ENT, String EJE, Integer CONCOD, String CGECOD);

    //needed for adding centro gestor to a contrato
    Boolean existsByENTAndEJEAndCONCODAndCGECOD(Integer ENT, String EJE, Integer CONCOD, String CGECOD);
}