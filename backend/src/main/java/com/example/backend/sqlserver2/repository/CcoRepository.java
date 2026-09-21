package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.example.backend.sqlserver2.model.Cco;
import com.example.backend.sqlserver2.model.CcoId;

import java.util.List;

@Repository
public interface CcoRepository extends JpaRepository<Cco, CcoId> {
    //selecting all centros de coste
    List<Cco> findByENTAndEJE(
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE
    );

    //search by ccocod also needed for adding
    List<Cco> findByENTAndEJEAndCCOCOD(
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE,
        @Param("CCOCOD") String CCOCOD
    );

    //search by ccodes
    List<Cco> findByENTAndEJEAndCCODESContaining(
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE,
        @Param("CCODES") String CCODES
    );

    //needed for adding a service
    long countByENTAndEJEAndCCOCOD(Integer ENT, String EJE, String CCOCOD);
}