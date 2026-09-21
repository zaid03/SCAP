package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.sqlserver2.model.Conn;
import com.example.backend.sqlserver2.model.ConId;

import java.util.Optional;

public interface ConRepository extends JpaRepository<Conn, ConId> {
    //for adding a contrato
    Optional<Conn> findFirstByENTAndEJEOrderByCONCODDesc(Integer ent, String eje);
}