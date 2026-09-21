package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Per;

@Repository
public interface PerRepository extends JpaRepository<Per, String> {

    //for search in personas first case
    List<Per> findByPERCODOrPERNOMContaining(String PERCOD, String PERNOM);

    //for search in personas second case
    List<Per> findByPERNOMContaining(String PERNOM);

    //selecting personas for servicios
    List<Per> findByPERCODIn(List<String> percods);
}