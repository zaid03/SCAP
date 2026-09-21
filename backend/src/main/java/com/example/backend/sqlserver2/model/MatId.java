package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class MatId implements Serializable {
    private Integer ENT;
    private Integer MAGCOD;
    private Integer MTACOD;

    public MatId() {}
    public MatId(Integer ENT, Integer MAGCOD, Integer MTACOD) {
        this.ENT = ENT;
        this.MAGCOD = MAGCOD;
        this.MTACOD = MTACOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getMAGCOD() {return MAGCOD;}
    public void setMAGCOD(Integer MAGCOD) {this.MAGCOD = MAGCOD;}

    public Integer getMTACOD() {return MTACOD;}
    public void setMTACOD(Integer MTACOD) {this.MTACOD = MTACOD;}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatId matId = (MatId) o;
        return Objects.equals(ENT, matId.ENT) &&
            Objects.equals(MAGCOD, matId.MAGCOD) &&
            Objects.equals(MTACOD, matId.MTACOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, MAGCOD, MTACOD);
    }
}