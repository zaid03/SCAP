package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;

@Entity
@IdClass(MtaId.class)
@Table(name = "MTA", schema = "dbo")
public class Mta {
    @Id
    private Integer ENT;

    @Id
    private Integer MTACOD;

    private String MTADES;

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public Integer getMTACOD() { return MTACOD; }
    public void setMTACOD(Integer MTACOD) { this.MTACOD = MTACOD; }
    
    public String getMTADES() { return MTADES; }
    public void setMTADES(String MTADES) { this.MTADES = MTADES; }
}
