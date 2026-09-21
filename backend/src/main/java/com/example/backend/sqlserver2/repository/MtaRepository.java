package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.model.MtaId;
import com.example.backend.dto.AlmacenajeAddDto;

@Repository
public interface MtaRepository extends JpaRepository<Mta, MtaId> {
    //to fetch all MTAs
    List<Mta> findByENT(Integer ent);

    //to fetch by ent and mtacod and to search in almacenajes
    List<Mta> findByENTAndMTACOD(Integer ent, Integer mtacod);
    Optional<Mta> findFirstByENTAndMTACOD(Integer ent, Integer mtacod);

    //to search in almacenajes
    List<Mta> findByENTAndMTADESContaining(Integer ent, String mtades);

    //needed for adding almacenaje
    List<AlmacenajeAddDto> findDtoByENT(Integer ent);
}
