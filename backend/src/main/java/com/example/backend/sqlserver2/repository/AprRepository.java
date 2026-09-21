package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Apr;
import com.example.backend.sqlserver2.model.AprId;

@Repository
public interface AprRepository extends JpaRepository<Apr, AprId> {
    //select all articulos
    List<Apr> findByENTAndTERCOD(Integer ent, Integer tercod);
}
