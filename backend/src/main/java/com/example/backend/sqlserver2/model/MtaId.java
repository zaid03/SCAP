package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class MtaId implements Serializable {
    private Integer ENT;
    private Integer MTACOD;

    public MtaId() {}
    public MtaId(Integer ENT, Integer MTACOD) {
        this.ENT = ENT;
        this.MTACOD = MTACOD;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public Integer getMTACOD() { return MTACOD; }
    public void setMTACOD(Integer MTACOD) { this.MTACOD = MTACOD; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MtaId mtaId = (MtaId) o;
        return Objects.equals(ENT, mtaId.ENT) &&
               Objects.equals(MTACOD, mtaId.MTACOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, MTACOD);
    }
}
