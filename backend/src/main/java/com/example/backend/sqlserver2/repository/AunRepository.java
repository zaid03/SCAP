package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Aun;
import com.example.backend.sqlserver2.model.AunId;

@Repository
public interface AunRepository extends JpaRepository<Aun, AunId> {
    //fetching main list and needed for select lists in mantinimiento general de articulos
    List<Aun> findByENT(Integer ent);

    //search function
    List<Aun> findByENTAndAUNCOD(Integer ent, String auncod);
    List<Aun> findByENTAndAUNDESContaining(Integer ent, String aundes);
}
