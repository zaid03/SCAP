package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.model.Asu;
import com.example.backend.sqlserver2.model.AsuId;
import com.example.backend.dto.ArticuloSubfamilia;

@Repository
public interface AsuRepository extends JpaRepository<Asu, AsuId> {
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

    //fetching subfamilias and search them and needed to add an articulo for consulta general
    List<Asu> findByENTAndAFACOD(int ent, String afacod);
    List<Asu> findByENTAndASUCOD(int ent, String asucod);

    //method to fetch articulos to add to proveedor and to find Asu records by ENT and ASUCOD like
    List<ArticuloSubfamilia> findByENTAndAFACODOrENTAndASUCOD(Integer ent, String afacod, Integer ent2, String asucod);
    List<ArticuloSubfamilia> findByENTAndASUDESContaining(int ent, String asudes);
    List<Asu> findAllByENTAndASUDESContaining(int ent, String asudes);

    //needed for deleting an almacenaje
    int countByENTAndMTACOD(Integer ent, Integer mtacod);

    //needed to add an articulo for consulta general
     @Query("""
        SELECT m.MAGCOD AS MAGCOD
        FROM Asu a
        JOIN Mat m
            ON a.ENT = m.ENT
        AND a.MTACOD = m.MTACOD
        WHERE a.ENT = :ent
        AND a.AFACOD = :afacod
        AND a.ASUCOD = :asucod
        """)
    List<magcodOnly> findMagcods(Integer ent, String afacod, String asucod);
}