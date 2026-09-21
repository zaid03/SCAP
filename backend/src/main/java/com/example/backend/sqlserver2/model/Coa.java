package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(CoaId.class)
@Table(name = "COA", schema = "dbo")
public class Coa {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private Integer CONCOD;

    @Id
    private String AFACOD;

    @Id
    private String ASUCOD;

    @Id
    private String ARTCOD;

    private Double COAPRE;

    private Double COAPR2;
    
    private Double COAPR3;

    private Double COAPR4;

    private Double COAPR5;

    @ManyToOne
    @JoinColumns({
    @JoinColumn(name="ENT", referencedColumnName="ENT", insertable=false, updatable=false),
    @JoinColumn(name="EJE", referencedColumnName="EJE", insertable=false, updatable=false),
    @JoinColumn(name="CONCOD", referencedColumnName="CONCOD", insertable=false, updatable=false)
    })
    private Conn conn;
    public Conn getConn() {return conn;}

    @ManyToOne
    @JoinColumns({
    @JoinColumn(name="ENT", referencedColumnName="ENT", insertable=false, updatable=false),
    @JoinColumn(name="AFACOD", referencedColumnName="AFACOD", insertable=false, updatable=false),
    @JoinColumn(name="ASUCOD", referencedColumnName="ASUCOD", insertable=false, updatable=false),
    @JoinColumn(name="ARTCOD", referencedColumnName="ARTCOD", insertable=false, updatable=false)
    })
    private Art art;
    public Art getArt() {return art;}

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    public Double getCOAPRE() {return COAPRE;}
    public void setCOAPRE(Double COAPRE) {this.COAPRE = COAPRE;}

    public Double getCOAPR2() {return COAPR2;}
    public void setCOAPR2(Double COAPR2) {this.COAPR2 = COAPR2;}

    public Double getCOAPR3() {return COAPR3;}
    public void setCOAPR3(Double COAPR3) {this.COAPR3 = COAPR3;}

    public Double getCOAPR4() {return COAPR4;}
    public void setCOAPR4(Double COAPR4) {this.COAPR4 = COAPR4;}

    public Double getCOAPR5() {return COAPR5;}
    public void setCOAPR5(Double COAPR5) {this.COAPR5 = COAPR5;}
}
