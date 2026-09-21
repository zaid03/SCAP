package com.example.backend.sqlserver2.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.dto.albFacturaDto;
import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.AlbId;

@Repository
public interface AlbRepository extends JpaRepository<Alb, AlbId> {
    //fetch albaranes for facturas
    List<Alb> findByENTAndEJEAndFACNUM(Integer ent, String eje, Integer facnum);

    //fetching albaranes for adding to a factura
    @Query(
        value = "SELECT T1.ALBREF AS ALBREF, T1.ALBDAT AS ALBDAT, T1.ALBBIM AS ALBBIM, T1.ALBNUM AS ALBNUM, T1.ALBFRE AS ALBFRE, T1.CONCTP AS CONCTP, T1.CONCPR AS CONCPR, T1.CONCCR AS CONCCR, " +
                "T2.DEPCOD AS DEPCOD, T1.ALBCOM AS ALBCOM " +
                "FROM ALB T1 " +
                "LEFT JOIN DEP T2 ON T1.ENT = T2.ENT AND T1.ALBCOM = T2.DEPCOD " +
                "WHERE T1.ENT = :ent " +
                "AND T2.EJE = :eje " +
                "AND T2.CGECOD = :cgecod " +
                "AND T1.FACNUM = :facnum " +
                "AND T1.TERCOD = :tercod",
        nativeQuery = true
    )
    List<albFacturaDto> findAlbFactura(
        @Param("ent") Integer ent,
        @Param("tercod") Integer tercod,
        @Param("facnum") Integer facnum,
        @Param("eje") String eje,
        @Param("cgecod") String cgecod
    );

    //searching in albaranes for adding to a factura
    @Query(
        value = "SELECT T1.ALBREF AS ALBREF, T1.ALBDAT AS ALBDAT, T1.ALBBIM AS ALBBIM, T1.ALBNUM AS ALBNUM, T1.ALBFRE AS ALBFRE, T1.CONCTP AS CONCTP, T1.CONCPR AS CONCPR, T1.CONCCR AS CONCCR, " +
                "T2.DEPCOD AS DEPCOD, T1.ALBCOM AS ALBCOM " +
                "FROM ALB T1 " +
                "LEFT JOIN DEP T2 ON T1.ENT = T2.ENT AND T1.ALBCOM = T2.DEPCOD " +
                "WHERE T1.ENT = :ent " +
                "AND T2.EJE = :eje " +
                "AND T2.CGECOD = :cgecod " +
                "AND T1.FACNUM = :facnum " +
                "AND T1.TERCOD = :tercod " +
                "AND T1.ALBDAT >= :albdat",
        nativeQuery = true
    )
    List<albFacturaDto> findAlbFacturaGreaterThanEqual(
        @Param("ent") Integer ent,
        @Param("tercod") Integer tercod,
        @Param("facnum") Integer facnum,
        @Param("albdat") LocalDateTime albdat,
        @Param("eje") String eje,
        @Param("cgecod") String cgecod
    );

    @Query(
        value = "SELECT T1.ALBREF AS ALBREF, T1.ALBDAT AS ALBDAT, T1.ALBBIM AS ALBBIM, T1.ALBNUM AS ALBNUM, T1.ALBFRE AS ALBFRE, T1.CONCTP AS CONCTP, T1.CONCPR AS CONCPR, T1.CONCCR AS CONCCR, " +
                "T2.DEPCOD AS DEPCOD, T1.ALBCOM AS ALBCOM " +
                "FROM ALB T1 " +
                "LEFT JOIN DEP T2 ON T1.ENT = T2.ENT AND T1.ALBCOM = T2.DEPCOD " +
                "WHERE T1.ENT = :ent " +
                "AND T2.EJE = :eje " +
                "AND T2.CGECOD = :cgecod " +
                "AND T1.FACNUM = :facnum " +
                "AND T1.TERCOD = :tercod " +
                "AND T1.ALBDAT <= :albdat",
        nativeQuery = true
    )
    List<albFacturaDto> findAlbFacturaLessThanEqual(
        @Param("ent") Integer ent,
        @Param("tercod") Integer tercod,
        @Param("facnum") Integer facnum,
        @Param("albdat") LocalDateTime albdat,
        @Param("eje") String eje,
        @Param("cgecod") String cgecod
    );
}