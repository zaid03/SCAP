package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Len;

import java.util.List;
@Repository
public interface LenRepository extends JpaRepository<Len, Integer> {

    // Get last record ordered by LENCOD descending
    Len findFirstByOrderByLENCODDesc();

    //search with lencod
    List<Len> findByLENCOD(@Param("LENCOD") Integer LENCOD);

    //search with lendes
    List<Len> findByLENDESContaining(@Param("LENDES") String LENDES);
}