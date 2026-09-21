package com.example.backend.sqlserver2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.sqlserver2.model.Fdt;
import com.example.backend.sqlserver2.model.FdtId;

@Repository
public interface FdtRepository extends JpaRepository<Fdt, FdtId> {
    //fetch descuentos of a factura and contabilizar a factura
    List<Fdt> findByENTAndEJEAndFACNUM(Integer ent, String eje, Integer facnum);
}
