package com.example.backend.sqlserver1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver1.model.Ayt;

@Repository
public interface AytRepository extends JpaRepository<Ayt, Integer> {
    //to fetch ws parameters
    List<Ayt> findByENTCOD(@Param("ENTCOD") Integer ENTCOD);
}