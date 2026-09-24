package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Sol;
import com.example.backend.sqlserver2.model.SolId;

@Repository
public interface SolRepository extends JpaRepository<Sol, SolId> {
    
}