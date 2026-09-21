package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.model.MtaId;

@Repository
public interface MtaRepository extends JpaRepository<Mta, MtaId> {
    //to fetch all MTAs
    List<Mta> findByENT(Integer ent);

    //to fetch by ent and mtacod
    List<Mta> findByENTAndMTACOD(Integer ent, Integer mtacod);
    Optional<Mta> findFirstByENTAndMTACOD(Integer ent, Integer mtacod);
}
