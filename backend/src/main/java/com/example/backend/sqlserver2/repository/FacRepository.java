package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.dto.FacWithTerProjection;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;

@Repository
public interface FacRepository extends JpaRepository<Fac, FacId>, JpaSpecificationExecutor<Fac>{
    //for the main list
    List<FacWithTerProjection> findByENTAndEJEAndCGECODOrderByFACFREAsc(Integer ent, String eje, String cgecod);
    List<FacWithTerProjection> findByENTAndEJEOrderByFACFREAsc(Integer ent, String eje);
    List<FacWithTerProjection> findByENTAndEJEAndCGECODAndFACADOIsNullOrderByFACFREAsc(Integer ent, String eje, String cgecod);

    //needed for adding a factura
    @Query("SELECT MAX(f.FACNUM) FROM Fac f WHERE f.ENT = :ent AND f.EJE = :eje")
    Integer findMaxFACNUMByENTAndEJE(@Param("ent") Integer ent, @Param("eje") String eje);

    List<Fac> findByFACTDCAndFACANNAndFACFAC(String FACTDC, Integer FACANN, Integer FACFAC);

    //selecting a factura for info updates upon a crud op done on facturas
    Optional<FacWithTerProjection> findByENTAndEJEAndFACNUM(Integer ent, String eje, Integer facnum);
}