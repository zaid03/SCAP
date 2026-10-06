package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.sqlserver2.model.Coh;
import com.example.backend.sqlserver2.model.CohId;
import com.example.backend.dto.HistoricaContratos;

import java.util.List;

public interface CohRepository extends JpaRepository<Coh, CohId> {
    //main fetch for historica de ad por contrato
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIP(Integer ent, String eje, Integer contip);

    //search for historica de ad por contrato
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCge_CGECOD(Integer ent, String eje, Integer contip, String cgecod);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndCon_CONCOD(Integer ent, String eje, Integer contip, String cgecod, Integer concod);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndCon_CONDES(Integer ent, String eje, Integer contip, String cgecod, String condes);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndCon_CONDESContaining(Integer ent, String eje, Integer contip, String cgecod, String condes);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_CONCOD(Integer ent, String eje, Integer contip, Integer concod);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_CONDES(Integer ent, String eje, Integer contip, String condes);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_CONDESContaining(Integer ent, String eje, Integer contip, String condes);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_Cots_Ter_TERCOD(Integer ent, String eje, Integer contip, Integer tercod);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_Cots_Ter_TERNIFContaining(Integer ent, String eje, Integer contip, String ternif);
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIPAndCon_Cots_Ter_TERNOMContaining(Integer ent, String eje, Integer contip, String ternom);
}