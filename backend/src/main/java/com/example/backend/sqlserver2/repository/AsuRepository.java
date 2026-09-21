package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.sqlserver2.model.Asu;
import com.example.backend.sqlserver2.model.AsuId;
import com.example.backend.dto.ArticuloSubfamilia;

@Repository
public interface AsuRepository extends JpaRepository<Asu, AsuId> {

    //method to fetch articulos to add to proveedor
    List<ArticuloSubfamilia> findByENTAndAFACODOrENTAndASUCOD(Integer ent, String afacod, Integer ent2, String asucod);
    List<ArticuloSubfamilia> findByENTAndASUDESContaining(int ent, String asudes);

    //find an art name to add subs
    List<Asu> findByENTAndAFACODAndASUCOD(int ENT, String AFACOD, String ASUCOD);

    //for deleting familias
    @Modifying
    @Transactional
    int deleteByENTAndAFACOD(Integer ent, String afacod);

    //to delete a subfamilia
    @Modifying
    @Transactional
    int deleteByENTAndAFACODAndASUCOD(Integer ENT, String AFACOD, String ASUCOD);

    //filtering subs by ent and afacod
    List<Asu> findByENTAndAFACOD(Integer ent, String afacod);
}
