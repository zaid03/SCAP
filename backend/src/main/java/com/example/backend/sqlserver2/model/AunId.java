package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AunId implements Serializable {
    private Integer ENT;
    private String AUNCOD;

    public AunId() {}
    public AunId(Integer ENT, String AUNCOD) {
        this.ENT = ENT;
        this.AUNCOD = AUNCOD;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getAUNCOD() {return AUNCOD;}
    public void setAUNCOD(String AUNCOD) {this.AUNCOD = AUNCOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AunId aunId = (AunId) o;
        return Objects.equals(ENT, aunId.ENT) &&
               Objects.equals(AUNCOD, aunId.AUNCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, AUNCOD);
    }
}
