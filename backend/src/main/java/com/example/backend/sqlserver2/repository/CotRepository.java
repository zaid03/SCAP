package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.service.CotContratoProjection;
import com.example.backend.sqlserver2.model.Cot;
import com.example.backend.sqlserver2.model.CotId;

public interface CotRepository extends JpaRepository<Cot, CotId> {
    //selecting all contratos and search todos
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJE(Integer CONTIP, Integer ent, String eje);

    //searching in contratos
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(Integer CONTIP, Integer ent, String eje, Integer concod);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(Integer CONTIP, Integer ent, String eje, String condes);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLONot(Integer CONTIP, Integer ent, String eje, Integer concod, Integer conblo);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLONot(Integer CONTIP, Integer ent, String eje, String condes, Integer conblo);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(Integer CONTIP, Integer ent, String eje, Integer conblo);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLO(Integer CONTIP, Integer ent, String eje, Integer concod, Integer conblo);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLO(Integer CONTIP, Integer ent, String eje, String condes, Integer conblo);
    List<CotContratoProjection> findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(Integer CONTIP, Integer ent, String eje, Integer conblo);

    //selecting one contratos for add
    Optional<CotContratoProjection> findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(Integer CONTIP, Integer ent, String eje, Integer concod);
}