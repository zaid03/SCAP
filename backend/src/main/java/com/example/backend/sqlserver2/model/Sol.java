package com.example.backend.sqlserver2.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;

@Entity
@IdClass(SolId.class)
@Table(name = "SOL", schema = "dbo")
public class Sol {
    @Id
    private Integer ENT;

    @Id
    private Integer SOLNUM;

    @Id
    private Integer SOLSUB;  

    private String EJE;
    
    private String PERCOD;

    private String SOLPER;

    private String DEPCOD;

    private Integer SOLEST;

    private Integer TERCOD;

    private LocalDateTime SOLDAL;

    private LocalDateTime SOLDTC;

    private LocalDateTime SOLDRR;

    private LocalDateTime SOLDTP;

    private LocalDateTime SOLDDE;

    private String SOLDES;

    private String SOLDSC;

    private Double SOLBIM;

    private Double SOLIVA;

    private Integer MRECOD;

    private Integer MRBCOD;

    private String SOLNRC;

    private String SOLNDI;

    private String SOLEJE;

    private Integer SOLIVD;

    private Integer SOLALB;

    private Integer SOLTIP;

    private Integer GRUCOD;

    private LocalDateTime SOLDDS;

    private LocalDateTime SOLDRE;

    private String SOLOBS;

    private String SOLLEN;

    private String SOLNOT;

    private Integer RELNUM;

    private Integer PRICOD;

    private Integer SOLENT;

    private String SOLDED;

    private Integer CONCOD;

    private Integer SOLDVP;

    private String CONCTP;

    private String CONCPR;

    private String CONCCR;

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getSOLNUM() {return SOLNUM;}
    public void setSOLNUM(Integer SOLSUB) {this.SOLNUM = SOLNUM;}

    public Integer getSOLSUB() {return SOLSUB;}
    public void setSOLSUB(Integer SOLSUB) {this.SOLSUB = SOLSUB;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public String getPERCOD() {return PERCOD;}
    public void setPERCOD(String PERCOD) {this.PERCOD = PERCOD;}

    public String getSOLPER() {return SOLPER;}
    public void setSOLPER(String SOLPER) {this.SOLPER = SOLPER;}

    public String getDEPCOD() {return DEPCOD;}
    public void setDEPCOD(String DEPCOD) {this.DEPCOD = DEPCOD;}

    public Integer getSOLEST() {return SOLEST;}
    public void setSOLEST(Integer SOLEST) {this.SOLEST = SOLEST;}

    public Integer getTERCOD() {return TERCOD;}
    public void setTERCOD(Integer TERCOD) {this.TERCOD = TERCOD;}

    public LocalDateTime getSOLDAL() {return SOLDAL;}
    public void setSOLDAL(LocalDateTime SOLDAL) {this.SOLDAL = SOLDAL;}

    public LocalDateTime getSOLDTC() {return SOLDTC;}
    public void setSOLDTC(LocalDateTime SOLDTC) {this.SOLDTC = SOLDTC;}

    public LocalDateTime getSOLDRR() {return SOLDRR;}
    public void setSOLDRR(LocalDateTime SOLDRR) {this.SOLDRR = SOLDRR;}

    public LocalDateTime getSOLDTP() {return SOLDTP;}
    public void setSOLDTP(LocalDateTime SOLDTP) {this.SOLDTP = SOLDTP;}

    public LocalDateTime getSOLDDE() {return SOLDDE;}
    public void setSOLDDE(LocalDateTime SOLDDE) {this.SOLDDE = SOLDDE;}

    public String getSOLDES() {return SOLDES;}
    public void setSOLDES(String SOLDES) {this.SOLDES = SOLDES;}

    public String getSOLDSC() {return SOLDSC;}
    public void setSOLDSC(String SOLDSC) {this.SOLDSC = SOLDSC;}

    public Double getSOLBIM() {return SOLBIM;}
    public void setSOLBIM(Double SOLBIM) {this.SOLBIM = SOLBIM;}

    public Double getSOLIVA() {return SOLIVA;}
    public void setSOLIVA(Double SOLIVA) {this.SOLIVA = SOLIVA;}

    public Integer getMRECOD() {return MRECOD;}
    public void setMRECOD(Integer MRECOD) {this.MRECOD = MRECOD;}

    public Integer getMRBCOD() {return MRBCOD;}
    public void setMRBCOD(Integer MRBCOD) {this.MRBCOD = MRBCOD;}

    public String getSOLNRC() {return SOLNRC;}
    public void setSOLNRC(String SOLNRC) {this.SOLNRC = SOLNRC;}

    public String getSOLNDI() {return SOLNDI;}
    public void setSOLNDI(String SOLNDI) {this.SOLNDI = SOLNDI;}

    public String getSOLEJE() {return SOLEJE;}
    public void setSOLEJE(String SOLEJE) {this.SOLEJE = SOLEJE;}

    public Integer getSOLIVD() {return SOLIVD;}
    public void setSOLIVD(Integer SOLIVD) {this.SOLIVD = SOLIVD;}

    public Integer getSOLALB() {return SOLALB;}
    public void setSOLALB(Integer SOLALB) {this.SOLALB = SOLALB;}

    public Integer getSOLTIP() {return SOLTIP;}
    public void setSOLTIP(Integer SOLTIP) {this.SOLTIP = SOLTIP;}

    public Integer getGRUCOD() {return GRUCOD;}
    public void setGRUCOD(Integer GRUCOD) {this.GRUCOD = GRUCOD;}

    public LocalDateTime getSOLDDS() {return SOLDDS;}
    public void setSOLDDS(LocalDateTime SOLDDS) {this.SOLDDS = SOLDDS;}

    public LocalDateTime getSOLDRE() {return SOLDRE;}
    public void setSOLDRE(LocalDateTime SOLDRE) {this.SOLDRE = SOLDRE;}

    public String getSOLOBS() {return SOLOBS;}
    public void setSOLOBS(String SOLOBS) {this.SOLOBS = SOLOBS;}

    public String getSOLLEN() {return SOLLEN;}
    public void setSOLLEN(String SOLLEN) {this.SOLLEN = SOLLEN;}

    public String getSOLNOT() {return SOLNOT;}
    public void setSOLNOT(String SOLNOT) {this.SOLNOT = SOLNOT;}

    public Integer getRELNUM() {return RELNUM;}
    public void setRELNUM(Integer RELNUM) {this.RELNUM = RELNUM;}

    public Integer getPRICOD() {return PRICOD;}
    public void setPRICOD(Integer PRICOD) {this.PRICOD = PRICOD;}

    public Integer getSOLENT() {return SOLENT;}
    public void setSOLENT(Integer SOLENT) {this.SOLENT = SOLENT;}

    public String getSOLDED() {return SOLDED;}
    public void setSOLDED(String SOLDED) {this.SOLDED = SOLDED;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public Integer getSOLDVP() {return SOLDVP;}
    public void setSOLDVP(Integer SOLDVP) {this.SOLDVP = SOLDVP;}

    public String getCONCTP() {return CONCTP;}
    public void setCONCTP(String CONCTP) {this.CONCTP = CONCTP;}

    public String getCONCPR() {return CONCPR;}
    public void setCONCPR(String CONCPR) {this.CONCPR = CONCPR;}

    public String getCONCCR() {return CONCCR;}
    public void setCONCCR(String CONCCR) {this.CONCCR = CONCCR;}
}