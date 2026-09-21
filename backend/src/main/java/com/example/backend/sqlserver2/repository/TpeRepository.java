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
    boolean existsByENTAndTERCODAndTPENOM(Integer ent, Integer tercod, String tpenom);

    Tpe findFirstByENTAndTERCODOrderByTPECODDesc(Integer ent, Integer tercod);
}
