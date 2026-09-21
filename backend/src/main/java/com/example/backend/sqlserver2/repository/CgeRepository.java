package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.sqlserver2.model.Cge;
import com.example.backend.sqlserver2.model.CgeId;
import com.example.backend.dto.shortCentroContrato;

@Repository
public interface CgeRepository extends JpaRepository<Cge, CgeId> {
    //to fetch all centro gestores
    List<Cge> findByENTAndEJE(int ent, String eje);

    //needed for adding a centro gestor 
    List<Cge> findByENTAndEJEAndCGECOD(int ent, String eje, String cgecod);

    //fetching description for services
    Optional<Cge> findFirstByENTAndEJEAndCGECOD(Integer ent, String eje, String cgecod);

    //selecting centro gestor for login
    List<Cge> findByENTAndEJEAndCGECODIn(Integer ent, String eje, List<String> cgecods);

    //for search in cge
    List<Cge> findByENTAndEJEAndCGECODOrENTAndEJEAndCGEDESContaining(
        Integer ent1, String eje1, String cgecod,
        Integer ent2, String eje2, String cgedes
    );

    //searching centos for contratos
    List<shortCentroContrato> findByENTAndEJEAndCGECOD(Integer ent, String eje, String cgecod);

    //searching centos for contratos
    List<shortCentroContrato> findByENTAndEJEAndCGEDESContaining(Integer ent, String eje, String cgedes);
}