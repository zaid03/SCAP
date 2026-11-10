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
import com.example.backend.dto.bolsaPorContrato;

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
    //needed for contabilizar a factura
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
        UPDATE g
        SET g.GBSICO = COALESCE(g.GBSICO, 0)
                        + COALESCE(fde.FDEIMP, 0)
                        + COALESCE(fde.FDEDIF, 0),
            g.GBSICT = COALESCE(g.GBSICT, 0)
                        + COALESCE(fde.FDEIMP, 0)
                        + COALESCE(fde.FDEDIF, 0)
        FROM dbo.GBS g
        INNER JOIN dbo.FDE fde
            ON g.ENT = fde.ENT
        AND g.EJE = fde.EJE
        AND g.GBSORG = fde.FDEORG
        AND g.GBSFUN = fde.FDEFUN
        AND g.GBSECO = fde.FDEECO
        INNER JOIN dbo.FAC fac
            ON fac.ENT = fde.ENT
        AND fac.EJE = fde.EJE
        AND fac.FACNUM = fde.FACNUM
        AND g.CGECOD = fac.CGECOD
        WHERE fde.ENT = :ent
        AND fde.EJE = :eje
        AND fde.FACNUM = :facnum
        """, nativeQuery = true)
    int acumular(
        @Param("ent") Integer ent,
        @Param("eje") String eje,
        @Param("facnum") Integer facnum
    );

    //for cambiar contrato to factura
    List<bolsaPorContrato> findAllByENTAndEJEAndCGECOD(Integer ent, String eje, String cgecod);
}