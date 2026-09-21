package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.sqlserver2.model.Afa;
import com.example.backend.sqlserver2.model.AfaId;

import com.example.backend.dto.ArticuloFamilia;

@Repository
public interface AfaRepository extends JpaRepository<Afa, AfaId> {

    // Method to find Afa records by ENT and AFACOD
    List<Afa> findByENTAndAFACOD(int ent, String afacod);    

    //find familias by ent
    List<Afa> findByENT(int ent);

    //deleting a familia
    @Modifying
    @Transactional
    int deleteByENTAndAFACOD(Integer ENT, String AFACOD);

    //method to fetch articulos to add to proveedor
    List<ArticuloFamilia> findAllByENTAndAFACOD(Integer ent, String afacod);
    List<ArticuloFamilia> findByENTAndAFADESContaining(int ent, String afades);
}
