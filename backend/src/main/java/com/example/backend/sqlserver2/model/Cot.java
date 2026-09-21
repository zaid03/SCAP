package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(CotId.class)
@Table(name = "COT", schema = "dbo")
public class Cot {
    @Id
    private Integer ENT;

    @Id
    private String EJE;

    @Id
    private Integer CONCOD;

    @Id
    private Integer TERCOD;

    private Integer COTLIN;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "EJE", referencedColumnName = "EJE", insertable = false, updatable = false),
        @JoinColumn(name = "CONCOD", referencedColumnName = "CONCOD", insertable = false, updatable = false)
    })
    private Conn conn;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "TERCOD", referencedColumnName = "TERCOD", insertable = false, updatable = false)
    })
    private Ter ter;
    
    public Conn getConn() { return conn; }
    public void setConn(Conn conn) { this.conn = conn; }

    public Ter getTer() { return ter; }
    public void setTer(Ter ter) { this.ter = ter; }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public Integer getTERCOD() {return TERCOD;}
    public void setTERCOD(Integer TERCOD) {this.TERCOD = TERCOD;}

    public Integer getCOTLIN() {return COTLIN;}
    public void setCOTLIN(Integer COTLIN) {this.COTLIN = COTLIN;}
}