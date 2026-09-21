package com.example.backend.sqlserver2.repository;

import com.example.backend.sqlserver2.model.Mat;
import com.example.backend.sqlserver2.model.MatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatRepository extends JpaRepository<Mat, MatId> {
    //selecting almacen
    List<Mat> findByENT(Integer ent);

    //seelcting almacen for servicios
    List<Mat> findByENTAndMAGCOD(Integer ent, Integer magcod);
}