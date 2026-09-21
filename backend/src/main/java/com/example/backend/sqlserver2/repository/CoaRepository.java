package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.sqlserver2.model.Coa;
import com.example.backend.sqlserver2.model.CoaId;
import com.example.backend.dto.CoaArtProjection;

import java.util.List;

public interface CoaRepository extends JpaRepository<Coa, CoaId> {
    //selecting articulos for a contrato
    List<CoaArtProjection> findAllByENTAndEJEAndConnCONCOD(Integer ent, String eje, Integer concod);

    // needed for saving articles for a contrato
    List<Coa> findByENTAndCONCODAndAFACODAndASUCODAndARTCOD(Integer ent, Integer concod, String afacod, String asucod, String artcod);
}