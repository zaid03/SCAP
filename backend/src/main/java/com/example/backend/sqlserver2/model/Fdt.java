package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(FdtId.class)
@Table(name = "FDT", schema = "dbo")
public class Fdt {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private Integer FACNUM;

    @Id 
    private Integer FDTLIN;

    private String FDTARE;
    
    private String FDTORG;

    private String FDTFUN;

    private String FDTECO;

    private Double FDTDTO;

    private Double FDTBSE;

    private Double FDTPRE;

    private String FDTTXT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false),
        @JoinColumn(name = "FACNUM", referencedColumnName = "FACNUM", insertable = false, updatable = false)
    })
    private Fac fac;

    public Fac getFac() { return fac; }
    public void setFac(Fac fac) { this.fac = fac; }
    
    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }
    
    public String getEJE() { return EJE; }
    public void setEJE(String EJE){ this.EJE = EJE; }

    public Integer getFACNUM() { return FACNUM; }
    public void setFACNUM(Integer FACNUM) { this.FACNUM = FACNUM; }

    public Integer getFDTLIN() { return FDTLIN; }
    public void setFDTLIN(Integer FDTLIN) { this.FDTLIN = FDTLIN; }

    public String getFDTARE() { return FDTARE; }
    public void setFDTARE(String FDTARE) { this.FDTARE = FDTARE; }

    public String getFDTORG() { return FDTORG; }
    public void setFDTORG(String FDTORG) { this.FDTORG = FDTORG; }

    public String getFDTFUN() { return FDTFUN; }
    public void setFDTFUN(String FDTFUN) { this.FDTFUN = FDTFUN; }

    public String getFDTECO() { return FDTECO; }
    public void setFDTECO(String FDTECO) { this.FDTECO = FDTECO; }

    public Double getFDTDTO() { return FDTDTO; }
    public void setFDTDTO(Double FDTDTO) { this.FDTDTO = FDTDTO; }

    public Double getFDTBSE() { return FDTBSE; }
    public void setFDTBSE(Double FDTBSE) { this.FDTBSE = FDTBSE; }

    public Double getFDTPRE() { return FDTPRE; }
    public void setFDTPRE(Double FDTPRE) { this.FDTPRE = FDTPRE; }

    public String getFDTTXT() { return FDTTXT; }
    public void setFDTTXT(String FDTTXT) { this.FDTTXT = FDTTXT; }
}