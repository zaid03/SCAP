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
        value = """
            SELECT DISTINCT
                ALB.ALBREF,
                ALB.ALBDAT,
                ALB.ALBBIM,
                ALB.ALBNUM,
                ALB.ALBFRE,
                ALB.DEPCOD,
                ALB.ALBCOM,
                ALB.CONCTP,
                ALB.CONCPR,
                ALB.CONCCR
            FROM ALB
            JOIN DEP
                ON ALB.ENT = DEP.ENT
            AND ALB.ALBCOM = DEP.DEPCOD
            JOIN SOL
                ON ALB.ENT = SOL.ENT
            AND ALB.SOLNUM = SOL.SOLNUM
            WHERE SOL.SOLSUB = 0
            AND SOL.CONCOD = :CONCOD
            AND ALB.ENT = :ENT
            AND ALB.EJE = :EJE
            AND DEP.CGECOD = :CGECOD
            AND ALB.FACNUM = 0
            AND ALB.TERCOD = :TERCOD
        """, nativeQuery = true
    )
    List<albFacturaDto> findAlbFactura(
        @Param("CONCOD") Integer CONCOD,
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE,
        @Param("CGECOD") String CGECOD,
        @Param("TERCOD") Integer TERCOD
    );

    //searching in albaranes for adding to a factura
    @Query(
        value = """
            SELECT DISTINCT
                ALB.ALBREF,
                ALB.ALBDAT,
                ALB.ALBBIM,
                ALB.ALBNUM,
                ALB.ALBFRE,
                ALB.DEPCOD,
                ALB.ALBCOM,
                ALB.CONCTP,
                ALB.CONCPR,
                ALB.CONCCR
            FROM ALB
            JOIN DEP
                ON ALB.ENT = DEP.ENT
            AND ALB.ALBCOM = DEP.DEPCOD
            JOIN SOL
                ON ALB.ENT = SOL.ENT
            AND ALB.SOLNUM = SOL.SOLNUM
            WHERE SOL.SOLSUB = 0
            AND SOL.CONCOD = :CONCOD
            AND ALB.ENT = :ENT
            AND ALB.EJE = :EJE
            AND DEP.CGECOD = :CGECOD
            AND ALB.FACNUM = 0
            AND ALB.TERCOD = :TERCOD
            AND ALB.ALBDAT >= :ALBDAT
        """, nativeQuery = true
    )
    List<albFacturaDto> findAlbFacturaGreaterThanEqual(
        @Param("CONCOD") Integer CONCOD,
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE,
        @Param("CGECOD") String CGECOD,
        @Param("TERCOD") Integer TERCOD,
        @Param("ALBDAT") LocalDateTime ALBDAT
    );

    @Query(
        value = """
            SELECT DISTINCT
                ALB.ALBREF,
                ALB.ALBDAT,
                ALB.ALBBIM,
                ALB.ALBNUM,
                ALB.ALBFRE,
                ALB.DEPCOD,
                ALB.ALBCOM,
                ALB.CONCTP,
                ALB.CONCPR,
                ALB.CONCCR
            FROM ALB
            JOIN DEP
                ON ALB.ENT = DEP.ENT
            AND ALB.ALBCOM = DEP.DEPCOD
            JOIN SOL
                ON ALB.ENT = SOL.ENT
            AND ALB.SOLNUM = SOL.SOLNUM
            WHERE SOL.SOLSUB = 0
            AND SOL.CONCOD = :CONCOD
            AND ALB.ENT = :ENT
            AND ALB.EJE = :EJE
            AND DEP.CGECOD = :CGECOD
            AND ALB.FACNUM = 0
            AND ALB.TERCOD = :TERCOD
            AND ALB.ALBDAT <= :ALBDAT
        """, nativeQuery = true
    )
    List<albFacturaDto> findAlbFacturaLessThanEqual(
        @Param("CONCOD") Integer CONCOD,
        @Param("ENT") Integer ENT,
        @Param("EJE") String EJE,
        @Param("CGECOD") String CGECOD,
        @Param("TERCOD") Integer TERCOD,
        @Param("ALBDAT") LocalDateTime ALBDAT
    );
}