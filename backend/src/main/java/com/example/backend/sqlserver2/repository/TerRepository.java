package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.model.TerId;

@Repository
public interface TerRepository extends JpaRepository<Ter, TerId>, JpaSpecificationExecutor<Ter> {
  //for the main list
  List<Ter> findByENT(int ent);

  //for selected proveedores to be added from sicalwin
  @Query(value = "SELECT ISNULL(MAX(TERCOD),0) + 1 FROM dbo.TER WITH (UPDLOCK, HOLDLOCK) WHERE ENT = :ent", nativeQuery = true)
  Integer findNextTercodForEnt(@Param("ent") int ent);

  //for the main list of facturas and contabilizar a factura
  Optional<Ter> findByENTAndTERCOD(Integer ent, Integer tercod);

  //needed for extracting tercod for adding facturas
  Ter findByENTAndTERNIF(Integer ent, String ternif);

  List<Ter> findAllByENTAndTERNIF(Integer ent, String ternif);
}