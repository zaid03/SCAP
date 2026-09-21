package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import com.example.backend.sqlserver2.model.Art;
import com.example.backend.sqlserver2.model.ArtId;
import com.example.backend.dto.ArtAsuContratoProjection;
import com.example.backend.dto.ArtNameProjection;
import com.example.backend.dto.ArticuloArticulo;
import com.example.backend.dto.AnaliticaArticulosProjectin;
import com.example.backend.dto.ArticleProjection;

@Repository
public interface ArtRepository extends JpaRepository<Art, ArtId> {
    //method to fetch articulos to add to proveedor
    List<ArticuloArticulo> findByENTAndAFACODOrENTAndASUCODOrENTAndARTCOD(Integer ent, String afacod, Integer ent2, String asucod, Integer ent3, String artcod);
    List<ArticuloArticulo> findByENTAndARTDESContaining(Integer ent, String artdes);

    //find an art name
    List<ArtNameProjection> findByENTAndAFACODAndASUCODAndARTCOD(int ent, String afacod, String asucod, String artcod);

    //delete a familia check
    Long countByENTAndAFACOD(Integer ent, String afacod);

    //delete a subfamilia check
    Long countByENTAndASUCOD(Integer ent, String asucod);

    //selecting articulos for contratos
    List<ArtAsuContratoProjection> findDistinctByENTAndAsuASUECO(Integer ent, String conlot);

    //searching in articulos for contratos by nums
    List<ArtAsuContratoProjection> findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(Integer ent1, String asueco1, String afacod, Integer ent2, String asueco2, String asucod);

    //searching in articulos for contratos by chars
    List<ArtAsuContratoProjection> findDistinctByENTAndAsuASUECOAndARTDESContaining(Integer ent, String conlot, String artdes);

    //needed for deleting a tipo de unidades
    int countByENTAndAUNCOD(Integer ent, String auncod);

    //main fetch for C.general existencias
    List<ArticleProjection> findByENTAndARTBLONot(Integer ent, Integer artblo);

    //searching in C.general existencias
    List<ArticleProjection> findByENTAndARTBLONotAndAFACODOrENTAndARTBLONotAndASUCOD(Integer ent1, Integer artblo1, String afacod, Integer ent2, Integer artblo2, String asucod);
    List<ArticleProjection> findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
        Integer ent1, Integer artblo1, String artcod,
        Integer ent2, Integer artblo2, String artdes,
        Integer ent3, Integer artblo3, String artref);
    List<ArticleProjection> findByENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
        Integer ent1, Integer artblo1, String artcod,
        Integer ent2, Integer artblo2, String artref
    );

    //search in consulta general de articulos with all criteria
    @Query(value = "SELECT " +
        "R.AFACOD, " +
        "F.AFADES as Afa_AFADES, " +
        "R.ASUCOD, " +
        "S.ASUDES as Asu_ASUDES, " +
        "R.ARTCOD, " +
        "R.ARTDES, " +
        "R.ARTREF, " +
        "R.ARTBLO, " +
        "R.ARTUNI, " +
        "R.ARTSOL, " +
        "R.ARTREC, " +
        "U.AUNDES as Aun_AUNDES, " +
        "R.ARTUCO, " +
        "R.ARTUEM, " +
        "R.ARTPMI, " +
        "R.ARTPMP, " +
        "R.ARTMIN, " +
        "R.ARTOPT " +
        "FROM art R " +
        "INNER JOIN afa F ON R.afacod = F.afacod AND R.ent = F.ent " +
        "INNER JOIN asu S ON R.asucod = S.asucod AND R.ent = S.ent AND R.afacod = S.afacod " +
        "INNER JOIN aun U ON R.auncod = U.auncod AND R.ent = U.ent " +
        "WHERE R.ent = :ent " +
        "AND (" +
            "(:search IS NULL OR :search = '') " +
            "OR (LEN(:search) < 11 AND R.ARTCOD = :search) " +
            "OR (UPPER(R.ARTDES) LIKE UPPER('%' + :search + '%')) " +
            "OR (LEN(:search) < 31 AND R.ARTREF = :search) " +
        ") " +
        "AND (:afacod IS NULL OR :afacod = '' OR R.AFACOD = :afacod) " +
        "AND (:asucod IS NULL OR :asucod = '' OR R.ASUCOD = :asucod) " +
        "AND (" +
            ":bloqueado = 'todos' " +
            "OR (:bloqueado = 'bloqueado' AND R.ARTBLO = 0) " +
            "OR (:bloqueado = 'nobloqueado' AND R.ARTBLO <> 0) " +
        ")", 
        nativeQuery = true
    )
    List<ArticleProjection> searchArticles(
        @Param("ent") Integer ent,
        @Param("search") String search,
        @Param("afacod") String afacod,
        @Param("asucod") String asucod,
        @Param("bloqueado") String bloqueado
    );

    //getting pagination number
    Integer countByENT(Integer ent);

    //main select for analitica de articulos
    List<AnaliticaArticulosProjectin> findByENT(Integer ent);

    //main fetch for consulta general de articulos and exporting data
    List<ArticleProjection> findByENT(Integer ent, Pageable pageable);
    List<ArticleProjection> findAllByENT(Integer ent);
}