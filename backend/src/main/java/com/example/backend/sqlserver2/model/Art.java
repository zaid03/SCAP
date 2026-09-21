package com.example.backend.sqlserver2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(ArtId.class)
@Table(name = "ART", schema = "dbo")
public class Art {
    
    @Id
    @Column(nullable = false)
    private Integer ENT;

    @Id
    @Column(nullable = false)
    private String AFACOD;

    @Id
    @Column(nullable = false)
    private String ASUCOD;

    @Id
    @Column(nullable = false)
    private String ARTCOD;

    @Column(nullable = true)
    private String ARTDES;

    private String ARTREF;

    private Integer ARTBLO;
    
    private Double ARTUNI;

    private Double ARTSOL;

    private Double ARTREC;

    private Double ARTUCO;

    private Double ARTUEM;

    private Double ARTPMI;

    private Double ARTPMP;

    private Double ARTMIN;

    private Double ARTOPT;


    private String AUNCOD;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "AFACOD", referencedColumnName = "AFACOD", insertable = false, updatable = false),
        @JoinColumn(name = "ASUCOD", referencedColumnName = "ASUCOD", insertable = false, updatable = false)
    })
    private Asu asu;
    public Asu getAsu() { return asu; }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "AFACOD", referencedColumnName = "AFACOD", insertable = false, updatable = false)
    })
    private Afa afa;
    public Afa getAfa() { return afa; }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "AUNCOD", referencedColumnName = "AUNCOD", insertable = false, updatable = false)
    })
    private Aun aun;
    public Aun getAun() { return aun; }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    public String getARTDES() {return ARTDES;}
    public void setARTDES(String ARTDES) {this.ARTDES = ARTDES;}

    public String getAUNCOD() {return AUNCOD;}
    public void setAUNCOD(String AUNCOD) {this.AUNCOD = AUNCOD;}

    public String getARTREF() {return ARTREF;}
    public void setARTREF(String ARTREF) {this.ARTREF = ARTREF;}

    public Integer getARTBLO() {return ARTBLO;}
    public void setARTBLO(Integer ARTBLO) {this.ARTBLO = ARTBLO;}

    public Double getARTUNI() {return ARTUNI;}
    public void setARTUNI(Double ARTUNI) {this.ARTUNI = ARTUNI;}

    public Double getARTSOL() {return ARTSOL;}
    public void setARTSOL(Double ARTSOL) {this.ARTSOL = ARTSOL;}

    public Double getARTREC() {return ARTREC;}
    public void setARTREC(Double ARTREC) {this.ARTREC = ARTREC;}

    public Double getARTUCO() {return ARTUCO;}
    public void setARTUCO(Double ARTUCO) {this.ARTUCO = ARTUCO;}

    public Double getARTUEM() {return ARTUEM;}
    public void setARTUEM(Double ARTUEM) {this.ARTUEM = ARTUEM;}

    public Double getARTPMI() {return ARTPMI;}
    public void setARTPMI(Double ARTPMI) {this.ARTPMI = ARTPMI;}

    public Double getARTPMP() {return ARTPMP;}
    public void setARTPMP(Double ARTPMP) {this.ARTPMP = ARTPMP;}

    public Double getARTMIN() {return ARTMIN;}
    public void setARTMIN(Double ARTMIN) {this.ARTMIN = ARTMIN;}

    public Double getARTOPT() {return ARTOPT;}
    public void setARTOPT(Double ARTOPT) {this.ARTOPT = ARTOPT;}
}
