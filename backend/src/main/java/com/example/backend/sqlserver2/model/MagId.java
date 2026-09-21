package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class MagId implements Serializable {
    private Integer ENT;
    private Integer MAGCOD;

    public MagId() {}
    public MagId(Integer ENT, Integer MAGCOD) {
        this.ENT = ENT;
        this.MAGCOD = MAGCOD;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public Integer getMAGCOD() { return MAGCOD; }
    public void setMAGCOD(Integer MAGCOD) { this.MAGCOD = MAGCOD; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagId magId = (MagId) o;
        return Objects.equals(ENT, magId.ENT) &&
               Objects.equals(MAGCOD, magId.MAGCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, MAGCOD);
    }
}
