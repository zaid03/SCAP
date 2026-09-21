package com.example.backend.sqlserver2.repository;

import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.model.Mag;
import com.example.backend.sqlserver2.model.MagId;
import com.example.backend.dto.magcodOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MagRepository extends JpaRepository<Mag, MagId> {
    //selecting almacen name
    Optional<magcodOnly> findByENTAndDEPCOD(Integer ent, String depcod);
}