package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Tpe;
import com.example.backend.sqlserver2.model.TpeId;

@Repository
public interface TpeRepository extends JpaRepository<Tpe, TpeId> {
    // Custom query to find Tpe by ENT and TERCOD
    List<Tpe> findByENTAndTERCOD(Integer ent, Integer tercod);

    //adding a persona
    boolean existsByENTAndTERCODAndTPETEL(Integer ent, Integer tercod, String tpetel);
    boolean existsByENTAndTERCODAndTPETMO(Integer ent, Integer tercod, String tpetmo);
    boolean existsByENTAndTERCODAndTPECOE(Integer ent, Integer tercod, String tpecoe);

    Tpe findFirstByENTAndTERCODOrderByTPECODDesc(Integer ent, Integer tercod);
}
