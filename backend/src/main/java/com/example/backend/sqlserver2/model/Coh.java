package com.example.backend.sqlserver2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(CohId.class)
@Table(name = "COH", schema = "dbo")
public class Coh {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private Integer CONCOD;

    @Id
    private Integer COHCOD;

    private String CGECOD;

    private String COHOPD;

    private String COHRFD;

    private LocalDateTime COHFEC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false),
        @JoinColumn(name = "CONCOD", referencedColumnName = "CONCOD", insertable = false, updatable = false)
    })
    private Conn con;
    public Conn getCon() {return con;}
    public void setCon(Conn con) {this.con = con;}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false),
        @JoinColumn(name = "CGECOD", referencedColumnName = "CGECOD", insertable = false, updatable = false)
    })
    private Cge cge;
    public Cge getCge() {return cge;}
    public void setCge(Cge cge) {this.cge = cge;}

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public Integer getCOHCOD() {return COHCOD;}
    public void setCOHCOD(Integer COHCOD) {this.COHCOD = COHCOD;}

    public String getCGECOD() {return CGECOD;}
    public void setCGECOD(String CGECOD) {this.CGECOD = CGECOD;}

    public String getCOHOPD() {return COHOPD;}
    public void setCOHOPD(String COHOPD) {this.COHOPD = COHOPD;}

    public String getCOHRFD() {return COHRFD;}
    public void setCOHRFD(String COHRFD) {this.COHRFD = COHRFD;}

    public LocalDateTime getCOHFEC() {return COHFEC;}
    public void setCOHFEC(LocalDateTime COHFEC) {this.COHFEC = COHFEC;}
}
