package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@IdClass(AlbId.class)
@Table(name = "ALB", schema = "dbo")
public class Alb {

    @Id
    private Integer ENT;

    @Id
    private Integer ALBNUM;

    private Integer SOLNUM;

    private Integer SOLSUB;

    private Integer TERCOD;

    private String ALBREF;

    private LocalDateTime ALBDAT;

    private String ALBOBS;

    private Double ALBBIM;

    private Double ALBIVA;

    private Integer ALBENT;

    private LocalDateTime ALBFRE;

    private String DEPCOD;

    private String ALBCOM;

    private String EJE;

    private Integer FACNUM;

    private String CONCTP;

    private String CONCPR;

    private String CONCCR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false),
        @JoinColumn(name = "FACNUM", referencedColumnName = "FACNUM", insertable = false, updatable = false)
    })
    private Fac fac;
    public Fac getFac() { return fac; }
    public void setFac(Fac fac) { this.fac = fac; }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "ALBCOM", referencedColumnName = "DEPCOD", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false)
    })
    private Dep dep;
    public Dep getDep() { return dep; }
    public void setDep(Dep dep) { this.dep = dep; }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public Integer getALBNUM() { return ALBNUM; }
    public void setALBNUM(Integer ALBNUM) { this.ALBNUM = ALBNUM; }

    public Integer getSOLNUM() { return SOLNUM; }
    public void setSOLNUM(Integer SOLNUM) { this.SOLNUM = SOLNUM; }

    public Integer getSOLSUB() { return SOLSUB; }
    public void setSOLSUB(Integer SOLSUB) { this.SOLSUB = SOLSUB; }

    public Integer getTERCOD() { return TERCOD; }
    public void setTERCOD(Integer TERCOD) { this.TERCOD = TERCOD; }

    public String getALBREF() { return ALBREF; }
    public void setALBREF(String ALBREF) { this.ALBREF = ALBREF; }

    public LocalDateTime getALBDAT() { return ALBDAT; }
    public void setALBDAT(LocalDateTime ALBDAT) { this.ALBDAT = ALBDAT; }

    public String getALBOBS() { return ALBOBS; }
    public void setALBOBS(String ALBOBS) { this.ALBOBS = ALBOBS; }

    public Double getALBBIM() { return ALBBIM; }
    public void setALBBIM(Double ALBBIM) { this.ALBBIM = ALBBIM; }

    public Double getALBIVA() { return ALBIVA; }
    public void setALBIVA(Double ALBIVA) { this.ALBIVA = ALBIVA; }

    public Integer getALBENT() { return ALBENT; }
    public void setALBENT(Integer ALBENT) { this.ALBENT = ALBENT; }

    public LocalDateTime getALBFRE() { return ALBFRE; }
    public void setALBFRE(LocalDateTime ALBFRE) { this.ALBFRE = ALBFRE; }

    public String getDEPCOD() { return DEPCOD; }
    public void setDEPCOD(String DEPCOD) { this.DEPCOD = DEPCOD; }

    public String getALBCOM() { return ALBCOM; }
    public void setALBCOM(String ALBCOM) { this.ALBCOM = ALBCOM; }

    public String getEJE() { return EJE; }
    public void setEJE(String EJE) { this.EJE = EJE; }

    public Integer getFACNUM() { return FACNUM; }
    public void setFACNUM(Integer FACNUM) { this.FACNUM = FACNUM; }

    public String getCONCTP() { return CONCTP; }
    public void setCONCTP(String CONCTP) { this.CONCTP = CONCTP; }

    public String getCONCPR() { return CONCPR; }
    public void setCONCPR(String CONCPR) { this.CONCPR = CONCPR; }

    public String getCONCCR() { return CONCCR; }
    public void setCONCCR(String CONCCR) { this.CONCCR = CONCCR; }
}