package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.model.GbsId;

@Repository
public interface  GbsRepository extends JpaRepository<Gbs, GbsId>{
    //for the main list of bolsa por cge and cambiar contrato sin contrato option
    List<Gbs> findByENTAndEJEAndCGECOD(int ent, String eje, String cgecod);

    //for deleting a centro gestor
    long countByENTAndEJEAndCGECOD(Integer ENT, String EJE, String CGECOD);

    //for main list of bolsa 
    List<Gbs> findByENTAndEJE(Integer ent, String eje);

    //needed for adding a bolsa
    Optional<Gbs> findByENTAndEJEAndCGECODAndGBSREFAndGBSORGAndGBSFUNAndGBSECO(Integer ENT, String EJE, String CGECOD, String GBSREF, String GBSORG, String GBSFUN, String GBSECO);

    //needed for contabilizar a factura
    Optional<Gbs> findByENTAndEJEAndCGECODAndGBSORGAndGBSFUNAndGBSECO(Integer ENT, String EJE, String CGECOD, String GBSORG, String GBSFUN, String GBSECO);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Gbs g SET g.GBSIUS = COALESCE(g.GBSIUS, 0) + :imp, g.GBSIUT = COALESCE(g.GBSIUT, 0) + :imp " + "WHERE g.ENT = :ent AND g.EJE = :eje AND g.CGECOD = :cge " + "AND g.GBSORG = :org AND g.GBSFUN = :fun AND g.GBSECO = :eco")
    int acumular(@Param("imp") double imp, @Param("ent") Integer ent, @Param("eje") String eje, @Param("cge") String cge, @Param("org") String org, @Param("fun") String fun, @Param("eco") String eco);
}