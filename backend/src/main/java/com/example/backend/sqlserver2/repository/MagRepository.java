package com.example.backend.sqlserver2.repository;

import com.example.backend.sqlserver2.model.Mag;
import com.example.backend.sqlserver2.model.MagId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MagRepository extends JpaRepository<Mag, MagId> {
    //selecting almacen name
    Optional<Mag> findByENTAndDEPCOD(Integer ent, String depcod);
}