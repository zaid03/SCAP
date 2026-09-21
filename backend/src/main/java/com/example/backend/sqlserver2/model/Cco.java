package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;

@Entity
@IdClass(CcoId.class)
@Table(name = "CCO", schema = "dbo")
public class Cco {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private String CCOCOD; 

    private String CCODES;

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getEJE() { return EJE; }
    public void setEJE(String EJE) { this.EJE = EJE; }

    public String getCCOCOD() { return CCOCOD; }
    public void setCCOCOD(String CCOCOD) { this.CCOCOD = CCOCOD; }

    public String getCCODES() { return CCODES; }
    public void setCCODES(String CCODES) { this.CCODES = CCODES; }
}
