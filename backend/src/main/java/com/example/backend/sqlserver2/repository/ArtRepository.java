package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.sqlserver2.model.Art;
import com.example.backend.sqlserver2.model.ArtId;
import com.example.backend.dto.ArtAsuContratoProjection;
import com.example.backend.dto.ArtNameProjection;
import com.example.backend.dto.ArticuloArticulo;

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
}