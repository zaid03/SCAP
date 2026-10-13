package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.dto.CoaArtProjection;
import com.example.backend.sqlserver2.model.Coa;
import com.example.backend.sqlserver2.model.CoaId;

public interface CoaRepository extends JpaRepository<Coa, CoaId> {
    //selecting articulos for a contrato
    List<CoaArtProjection> findAllByENTAndEJEAndConnCONCOD(Integer ent, String eje, Integer concod);

    // needed for saving articles for a contrato
    List<Coa> findByENTAndEJEAndCONCODAndAFACODAndASUCODAndARTCOD(Integer ent, String eje, Integer concod, String afacod, String asucod, String artcod);
}