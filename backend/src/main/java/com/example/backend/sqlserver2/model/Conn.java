package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@IdClass(ConId.class)
@Table(name = "CON", schema = "dbo")
public class Conn {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private Integer CONCOD;

    private String CONLOT;

    private String CONDES;

    private Integer CONTIP;

    private Integer CONPRE;

    private LocalDateTime CONFIN;

    private LocalDateTime CONFFI;

    private Double CONIMP;

    private Integer CONBLO;

    private String CONCTP;

    private String CONCPR;

    private String CONCCR;

    @OneToMany(mappedBy = "conn")
    @JsonIgnore
    private List<Cot> cots;

    public List<Cot> getCots() { return cots; }
    public void setCots(List<Cot> cots) { this.cots = cots; }
    
    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public String getCONLOT() {return CONLOT;}
    public void setCONLOT(String CONLOT) {this.CONLOT = CONLOT;}

    public String getCONDES() {return CONDES;}
    public void setCONDES(String CONDES) {this.CONDES = CONDES;}

    public Integer getCONTIP() {return CONTIP;}
    public void setCONTIP(Integer CONTIP) {this.CONTIP = CONTIP;}

    public Integer getCONPRE() {return CONPRE;}
    public void setCONPRE(Integer CONPRE) {this.CONPRE = CONPRE;}

    public LocalDateTime getCONFIN() {return CONFIN;}
    public void setCONFIN(LocalDateTime CONFIN) {this.CONFIN = CONFIN;}

    public LocalDateTime getCONFFI() {return CONFFI;}
    public void setCONFFI(LocalDateTime CONFFI) {this.CONFFI = CONFFI;}

    public Double getCONIMP() {return CONIMP;}
    public void setCONIMP(Double CONIMP) {this.CONIMP = CONIMP;}

    public Integer getCONBLO() {return CONBLO;}
    public void setCONBLO(Integer CONBLO) {this.CONBLO = CONBLO;}

    public String getCONCTP() {return CONCTP;}
    public void setCONCTP(String CONCTP) {this.CONCTP = CONCTP;}

    public String getCONCPR() {return CONCPR;}
    public void setCONCPR(String CONCPR) {this.CONCPR = CONCPR;}

    public String getCONCCR() {return CONCCR;}
    public void setCONCCR(String CONCCR) {this.CONCCR = CONCCR;}
}