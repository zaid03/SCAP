package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.FdeId;
import com.example.backend.dto.FdeFacTerProjection;

@Repository
public interface FdeRepository extends JpaRepository<Fde, FdeId> {
    //fetching applicaciones for a factura and contabilizar a factura
    List<Fde> findByENTAndEJEAndFACNUM(Integer ent, String eje, Integer facnum);

    //needed for quitar albaranes
    Optional<Fde> findByENTAndEJEAndFACNUMAndFDEECO(Integer ent, String eje, Integer facnum, String fdeeco);

    //selecting all facturas in consulta de del contabilizado
    @Query(value = """
        SELECT T1.FACNUM, T1.FDEREF, T1.FDEOPE, T1.FDEORG, T1.FDEFUN, T1.FDEECO, T1.FDESUB, T1.FDEIMP, T1.FDEDIF,
            T2.CGECOD, T2.FACDOC, T2.FACFCO, T2.TERCOD, T2.FACIMP, T2.FACANN, T2.FACFAC, T2.FACDAT,
            T3.TERNOM, T3.TERNIF
        FROM dbo.FDE T1
        INNER JOIN dbo.FAC T2 ON T1.ENT = T2.ENT AND T1.EJE = T2.EJE AND T1.FACNUM = T2.FACNUM
        INNER JOIN dbo.TER T3 ON T2.ENT = T3.ENT AND T2.TERCOD = T3.TERCOD
        WHERE T1.ENT = :ent 
        AND T1.EJE = :eje
        AND T2.FACFCO IS NULL
        AND (T1.FDEIMP > 0 OR T1.FDEDIF > 0)
        """, nativeQuery = true)
    List<ProjectionContabilizar> findPendienteContabilizar(Integer ent, String eje);

    //selecting all facturas in consulta de del contabilizado
    List<FdeFacTerProjection> findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(Integer ent1, String eje1, Double fdeimp, Integer ent2, String eje2, Double fdedif);
}