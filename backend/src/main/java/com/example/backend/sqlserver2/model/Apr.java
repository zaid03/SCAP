package com.example.backend.sqlserver2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(AprId.class)
@Table(name = "APR", schema = "dbo")
public class Apr {
    
    @Id
    private Integer ENT;
    @Id
    private Integer TERCOD;
    @Id
    private String AFACOD;
    @Id
    private String ASUCOD;
    @Id
    private String ARTCOD;

    @Column(nullable = true)
    private String APRREF;

    @Column(nullable = true)
    private Double APRPRE;

    @Column(nullable = true)
    private Double APRUEM;

    @Column(nullable = true)
    private String APROBS;

    @Column(nullable = true)
    private Integer APRACU;

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getTERCOD() {return TERCOD;}
    public void setTERCOD(Integer TERCOD) {this.TERCOD = TERCOD;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    public String getAPRREF() {return APRREF;}
    public void setAPRREF(String APRREF) {this.APRREF = APRREF;}

    public Double getAPRPRE() {return APRPRE;}
    public void setAPRPRE(Double APRPRE) {this.APRPRE = APRPRE;}

    public Double getAPRUEM() {return APRUEM;}
    public void setAPRUEM(Double APRUEM) {this.APRUEM = APRUEM;}

    public String getAPROBS() {return APROBS;}
    public void setAPROBS(String APROBS) {this.APROBS = APROBS;}

    public Integer getAPRACU() {return APRACU;}
    public void setAPRACU(Integer APRACU) {this.APRACU = APRACU;}
}
