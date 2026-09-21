package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AfaId implements Serializable {
    private Integer ENT;
    private String AFACOD;

    public AfaId() {}

    public AfaId(Integer ENT, String AFACOD) {
        this.ENT = ENT;
        this.AFACOD = AFACOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AfaId afaId = (AfaId) o;
        return Objects.equals(ENT, afaId.ENT) &&
               Objects.equals(AFACOD, afaId.AFACOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, AFACOD);
    }
}
