package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@IdClass(MeaId.class)
@Table(name = "MEA", schema = "dbo")
public class Mea {
    @Id
    private Integer ENT;

    @Id
    private Integer MAGCOD;

    @Id
    private String AFACOD;

    @Id
    private String ASUCOD;

    @Id
    private String ARTCOD;

    private Double MEAUNI;

    private String MEALOC;

    private Double MEAMIN;

    private Double MEASOL;

    private Double MEAOPT;

    private Double MEAPMP;

    private Double MEAPMI;

    private Double MEAREC;

    private String MEAMEN;

    private Integer MEAIND;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "MAGCOD", referencedColumnName = "MAGCOD", insertable = false, updatable = false)
    })
    public Mag mag;
    public Mag getMag() {return mag;}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AFACOD", referencedColumnName = "AFACOD", insertable = false, updatable = false),
        @JoinColumn(name = "ASUCOD", referencedColumnName = "ASUCOD", insertable = false, updatable = false),
        @JoinColumn(name = "ARTCOD", referencedColumnName = "ARTCOD", insertable = false, updatable = false)
    })
    public Art art;
    public Art getArt() {return art;}

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getMAGCOD() {return MAGCOD;}
    public void setMAGCOD(Integer MAGCOD) {this.MAGCOD = MAGCOD;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;} 

    public Double getMEAUNI() {return MEAUNI;}
    public void setMEAUNI(Double MEAUNI) {this.MEAUNI = MEAUNI;}

    public String getMEALOC() {return MEALOC;}
    public void setMEALOC(String MEALOC) {this.MEALOC = MEALOC;}

    public Double getMEAMIN() {return MEAMIN;}
    public void setMEAMIN(Double MEAMIN) {this.MEAMIN = MEAMIN;}

    public Double getMEASOL() {return MEASOL;}
    public void setMEASOL(Double MEASOL) {this.MEASOL = MEASOL;}

    public Double getMEAOPT() {return MEAOPT;}
    public void setMEAOPT(Double MEAOPT) {this.MEAOPT = MEAOPT;}

    public Double getMEAPMP() {return MEAPMP;}
    public void setMEAPMP(Double MEAPMP) {this.MEAPMP = MEAPMP;}

    public Double getMEAPMI() {return MEAPMI;}
    public void setMEAPMI(Double MEAPMI) {this.MEAPMI = MEAPMI;}

    public Double getMEAREC() {return MEAREC;}
    public void setMEAREC(Double MEAREC) {this.MEAREC = MEAREC;}

    public String getMEAMEN() {return MEAMEN;}
    public void setMEAMEN(String MEAMEN) {this.MEAMEN = MEAMEN;}

    public Integer getMEAIND() {return MEAIND;}
    public void setMEAIND(Integer MEAIND) {this.MEAIND = MEAIND;}
}