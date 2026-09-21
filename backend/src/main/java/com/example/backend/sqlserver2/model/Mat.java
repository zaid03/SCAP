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
@IdClass(MatId.class)
@Table(name = "MAT", schema = "dbo")
public class Mat {
    @Id
    private Integer ENT;

    @Id
    private Integer MAGCOD;

    @Id
    private Integer MTACOD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "MAGCOD", referencedColumnName = "MAGCOD", insertable = false, updatable = false)
    })
    private Mag mag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "MTACOD", referencedColumnName = "MTACOD", insertable = false, updatable = false)
    })
    private Mta mta;

    public Mag getMag() {return mag;}
    public void setMag(Mag mag) {this.mag = mag;}

    public Mta getMta() {return mta;}
    public void setMta(Mta mta) {this.mta = mta;}

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getMAGCOD() {return MAGCOD;}
    public void setMAGCOD(Integer MAGCOD) {this.MAGCOD = MAGCOD;}

    public Integer getMTACOD() {return MTACOD;}
    public void setMTACOD(Integer MTACOD) {this.MTACOD = MTACOD;}
}
