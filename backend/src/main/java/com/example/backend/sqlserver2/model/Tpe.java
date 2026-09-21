package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(TpeId.class)
@Table(name = "TPE", schema = "dbo")
public class Tpe {
    
    @Id
    private Integer ENT;

    @Id
    private Integer TERCOD;

    @Id
    private Integer TPECOD;

    private String TPENOM;

    private String TPETEL;

    private String TPETMO;

    private String TPECOE;

    private String TPEOBS;

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getTERCOD() {return TERCOD;}
    public void setTERCOD(Integer TERCOD) {this.TERCOD = TERCOD;}

    public Integer getTPECOD() {return TPECOD;}
    public void setTPECOD(Integer TPECOD) {this.TPECOD = TPECOD; }

    public String getTPENOM() {return TPENOM;}
    public void setTPENOM(String TPENOM) {this.TPENOM = TPENOM;}

    public String getTPETEL() {return TPETEL;}
    public void setTPETEL(String TPETEL) {this.TPETEL = TPETEL;}

    public String getTPETMO() {return TPETMO;}
    public void setTPETMO(String TPETMO) {this.TPETMO = TPETMO;}

    public String getTPECOE() {return TPECOE;}
    public void setTPECOE(String TPECOE) {this.TPECOE = TPECOE;}

    public String getTPEOBS() {return TPEOBS;}
    public void setTPEOBS(String TPEOBS) {this.TPEOBS = TPEOBS;}
}
