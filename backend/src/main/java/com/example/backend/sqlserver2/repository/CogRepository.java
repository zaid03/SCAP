package com.example.backend.sqlserver2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.dto.COGAIPOnlyDto;
import com.example.backend.dto.CogCgeProjection;
import com.example.backend.dto.SaldoContrato;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.CogId;

public interface CogRepository extends JpaRepository<Cog, CogId> {
  //selecting centro gestores for contrato
  List<CogCgeProjection> findAllByENTAndEJEAndCONCOD(Integer ENT, String EJE, Integer CONCOD);

  //needed for deleting a centro gestor from a contrato
  Optional<COGAIPOnlyDto> findByENTAndEJEAndCONCODAndCGECOD(Integer ENT, String EJE, Integer CONCOD, String CGECOD);

  //needed for adding centro gestor to a contrato
  Boolean existsByENTAndEJEAndCONCODAndCGECOD(Integer ENT, String EJE, Integer CONCOD, String CGECOD);

  //main fetch for C.saldo de contrato
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONot(Integer ent, String eje, Integer contip, Integer conblo);

  //filtering for C.saldo de contrato
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECOD(Integer ent, String eje, Integer contip, Integer conblo, String cgecod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(Integer ent, String eje, Integer contip, Integer conblo, Integer concod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_conn_CONDESContaining(Integer ent, String eje, Integer contip, Integer conblo, String condes);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERCODOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(Integer ent1, String eje1, Integer contip1, Integer conblo1, Integer tercod, Integer ent2, String eje2, Integer contip2, Integer conblo2, String ternif);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNOMContainingOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(Integer ent1, String eje1, Integer contip1, Integer conblo1, String ternom, Integer ent2, String eje2, Integer contip2, Integer conblo2, String ternif);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCONCOD(Integer ent, String eje, Integer contip, Integer conblo, String cgecod, Integer concod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCot_conn_CONDESContaining(Integer ent, String eje, Integer contip, Integer conblo, String cgecod, String condes);

  //main fetch for historica de ad
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIP(Integer ent, String eje, Integer contip);

  //filtering for historica de ad
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCge_CGECOD(Integer ent, String eje, Integer contip, String cgecod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCONCOD(Integer ent, String eje, Integer contip, Integer concod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONDESContaining(Integer ent, String eje, Integer contip, String condes);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_ter_TERCODOrENTAndEJEAndCot_conn_CONTIPAndCot_ter_TERNIFContaining(Integer ent1, String eje1, Integer contip1, Integer tercod, Integer ent2, String eje2, Integer contip2, String ternif);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCot_ter_TERNOMContainingOrENTAndEJEAndCot_conn_CONTIPAndCot_ter_TERNIFContaining(Integer ent1, String eje1, Integer contip1, String ternom, Integer ent2, String eje2, Integer contip2, String ternif);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCge_CGECODAndCONCOD(Integer ent, String eje, Integer contip, String cgecod, Integer concod);
  List<SaldoContrato> findByENTAndEJEAndCot_conn_CONTIPAndCge_CGECODAndCot_conn_CONDESContaining(Integer ent, String eje, Integer contip, String cgecod, String condes);

  //cambiar contrato with a the option of selecting contrato
  List<CogCgeProjection> findAllByENTAndEJEAndCONCODAndCGECOD(Integer ent, String eje, Integer concod, String cgecod);

  //needed for contabilizacion
  Optional<Cog> findOneByENTAndEJEAndCONCODAndCGECOD(Integer ent, String eje, Integer concod, String cgecod);

  Optional<Cog> findCogByENTAndEJEAndCONCODAndCGECOD(Integer ent, String eje, Integer concod, String cgecod);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Cog c SET c.COGIAP = COALESCE(c.COGIAP, 0) - :imp " + "WHERE c.ENT = :ent AND c.EJE = :eje AND c.CONCOD = :con AND c.CGECOD = :cge")
  int restarPedidosPendientes(@Param("imp") double imp, @Param("ent") Integer ent, @Param("eje") String eje, @Param("con") Integer con, @Param("cge") String cge);
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Cog c SET c.COGIMP = :saldo WHERE c.ENT = :ent AND c.EJE = :eje AND c.CONCOD = :con AND c.CGECOD = :cge")
  int actualizarSaldoPrincipal(@Param("saldo") double saldo, @Param("ent") Integer ent, @Param("eje") String eje,  @Param("con") Integer con, @Param("cge") String cge);
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Cog c SET c.COGIM2 = :saldo WHERE c.ENT = :ent AND c.EJE = :eje AND c.CONCOD = :con AND c.CGECOD = :cge")
  int actualizarSaldoSecundario(@Param("saldo") double saldo, @Param("ent") Integer ent, @Param("eje") String eje, @Param("con") Integer con, @Param("cge") String cge);
}