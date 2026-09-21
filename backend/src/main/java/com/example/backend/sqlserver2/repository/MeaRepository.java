package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.dto.ArticulosPorAlmcenProjection;
import com.example.backend.dto.existenciasProjection;
import com.example.backend.sqlserver2.model.Mea;
import com.example.backend.sqlserver2.model.MeaId;

@Repository
public interface MeaRepository extends JpaRepository<Mea, MeaId> {
    //selecting articulos por almacen and exporting
    List<ArticulosPorAlmcenProjection> findByENT(Integer ent, Pageable pageable);
    List<ArticulosPorAlmcenProjection> findAllByENT(Integer ent);

    //getting pagination number
    Integer countByENT(Integer ent);
    
    //search query for articulos por almacen
    @Query(value = """
        SELECT 
            afa.AFACOD as Art_Afa_AFACOD,
            afa.AFADES as Art_Afa_AFADES,
            asu.ASUCOD as Art_Asu_ASUCOD,
            asu.ASUDES as Art_Asu_ASUDES,
            art.ARTCOD as Art_ARTCOD,
            art.ARTDES as Art_ARTDES,
            art.ARTREF as Art_ARTREF,
            art.ARTBLO as Art_ARTBLO,
            art.ARTUNI as Art_ARTUNI,
            mea.MEAUNI as MEAUNI,
            mea.MEASOL as MEASOL,
            mea.MEAREC as MEAREC,
            aun.AUNDES as Art_Aun_AUNDES,
            art.ARTUCO as Art_ARTUCO,
            art.ARTUEM as Art_ARTUEM,
            art.ARTPMI as Art_ARTPMI,
            mea.MEAPMP as MEAPMP,
            mea.MEAMIN as MEAMIN,
            mea.MEAOPT as MEAOPT
        FROM dbo.MEA mea
        INNER JOIN dbo.ART art ON mea.ARTCOD = art.ARTCOD 
            AND mea.ASUCOD = art.ASUCOD 
            AND mea.AFACOD = art.AFACOD
        INNER JOIN dbo.AFA afa ON art.AFACOD = afa.AFACOD 
            AND art.ENT = afa.ENT
        INNER JOIN dbo.ASU asu ON art.ASUCOD = asu.ASUCOD 
            AND art.AFACOD = asu.AFACOD 
            AND art.ENT = asu.ENT
        LEFT JOIN dbo.AUN aun ON art.AUNCOD = aun.AUNCOD 
            AND art.ENT = aun.ENT
        WHERE mea.ENT = ?1
            AND (?2 IS NULL OR (
                art.ARTCOD = ?2
                OR art.ARTDES LIKE CONCAT('%', ?2, '%')
                OR art.ARTREF = ?2
            ))
            AND (?3 IS NULL OR afa.AFACOD = ?3)
            AND (?4 IS NULL OR asu.ASUCOD = ?4)
            AND (
                ?5 = 'Todos'
                OR (?5 = 'No bloqueados' AND art.ARTBLO <> 0)
                OR (?5 = 'Bloqueados' AND art.ARTBLO = 0)
            )
            AND (?6 IS NULL OR EXISTS (
                SELECT NULL FROM dbo.MEA ST1
                INNER JOIN dbo.MAG ST2 ON ST2.ENT = ST1.ENT AND ST2.MAGCOD = ST1.MAGCOD
                WHERE art.ENT = ST1.ENT
                  AND art.AFACOD = ST1.AFACOD
                  AND art.ASUCOD = ST1.ASUCOD
                  AND art.ARTCOD = ST1.ARTCOD
                  AND ST2.DEPCOD = ?6
            ))
        """, 
        nativeQuery = true)
    List<ArticulosPorAlmcenProjection> searchArticulos(
        Integer ent,
        String mainSearch,
        String afaCod,
        String asuCod,
        String bloqueado,
        String almacen
    );

    //selecting existencias for articles with eager loading of relationships
    @EntityGraph(attributePaths = {"mag", "mag.dep"})
    List<Mea> findByENTAndAFACODAndASUCODAndARTCOD(Integer ent, String afacod, String asucod, String artcod);

    //selecting and searching in C.existencias por almacen
    List<existenciasProjection> findByENTAndMAGCODAndArt_ARTBLONot(Integer ent, Integer magcod, Integer artblo, Pageable pageable);
    List<existenciasProjection> findAllByENTAndMAGCODAndArt_ARTBLONot(Integer ent, Integer magcod, Integer artblo);
    List<existenciasProjection> findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACOD(Integer ent, Integer magcod, Integer artblo, String afacod);
    List<existenciasProjection> findByENTAndMAGCODAndArt_ARTBLONotAndArt_ASUCOD(Integer ent, Integer magcod, Integer artblo, String asucod);
    List<existenciasProjection> findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACODAndArt_ASUCOD(Integer ent, Integer magcod, Integer artblo, String afacod, String asucod);

    //getting total pages for it
    long countByMAGCODAndArt_ARTBLONot(Integer magcod, Integer artblo);
}