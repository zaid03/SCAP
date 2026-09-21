package com.example.backend.sqlserver2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.backend.dto.AsuEcoImpProjection;
import com.example.backend.sqlserver2.model.Ade;
import com.example.backend.sqlserver2.model.AdeId;

@Repository
public interface AdeRepository extends JpaRepository<Ade, AdeId>{
    @Query(value = """
        SELECT s.ASUECO AS asueco, 
               SUM(ROUND(ROUND(a.ADEPRE, 4) * ROUND(a.ADEUNI, 2), 2)) AS imp
        FROM dbo.ADE a
        JOIN dbo.ASU s ON a.ENT = s.ENT AND a.AFACOD = s.AFACOD AND a.ASUCOD = s.ASUCOD
        WHERE a.ENT = :ent AND a.ALBNUM = :albnum
        GROUP BY s.ASUECO
        """, nativeQuery = true)
    Optional<AsuEcoImpProjection> findSumByEntAndAlbnum(@Param("ent") Integer ent, @Param("albnum") Integer albnum);
}