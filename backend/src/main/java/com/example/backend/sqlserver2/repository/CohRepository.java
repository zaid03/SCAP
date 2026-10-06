package com.example.backend.sqlserver2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.sqlserver2.model.Coh;
import com.example.backend.sqlserver2.model.CohId;
import com.example.backend.dto.HistoricaContratos;

import java.util.List;

public interface CohRepository extends JpaRepository<Coh, CohId> {
    //main fetch for historica de ad por contrato
    List<HistoricaContratos> findByENTAndEJEAndCon_CONTIP(Integer ent, String eje, Integer contip);
}