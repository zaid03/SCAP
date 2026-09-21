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
@IdClass(AdeId.class)
@Table(name = "ADE", schema = "dbo")
public class Ade {
    @Id
    private Integer ENT;

    @Id
    private Integer ALBNUM;

    @Id
    private Integer ADENUM;

    private String AFACOD;

    private String ASUCOD;

    private String ARTCOD;

    private Double ADEPRE;

    private Double ADEUNI;

    private Double ADEIVA;

    private Double ADEDTO;

    private String ADEREF;

    private String ADEOBS;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "AFACOD", referencedColumnName = "AFACOD", insertable = false, updatable = false),
        @JoinColumn(name = "ASUCOD", referencedColumnName = "ASUCOD", insertable = false, updatable = false)
    })
    private Asu asu;
    public Asu getAsu() { return asu; }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getALBNUM() {return ALBNUM;}
    public void setALBNUM(Integer ALBNUM) {this.ALBNUM = ALBNUM;}

    public Integer getADENUM() {return ADENUM;}
    public void setADENUM(Integer ADENUM) {this.ADENUM = ADENUM;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    public Double getADEPRE() {return ADEPRE;}
    public void setADEPRE(Double ADEPRE) {this.ADEPRE = ADEPRE;}

    public Double getADEUNI() {return ADEUNI;}
    public void setADEUNI(Double ADEUNI) {this.ADEUNI = ADEUNI;}

    public Double getADEIVA() {return ADEIVA;}
    public void setADEIVA(Double ADEIVA) {this.ADEIVA = ADEIVA;}

    public Double getADEDTO() {return ADEDTO;}
    public void setADEDTO(Double ADEDTO) {this.ADEDTO = ADEDTO;}

    public String getADEREF() {return ADEREF;}
    public void setADEREF(String ADEREF) {this.ADEREF = ADEREF;}

    public String getADEOBS() {return ADEOBS;}
    public void setADEOBS(String ADEOBS) {this.ADEOBS = ADEOBS;}
}
