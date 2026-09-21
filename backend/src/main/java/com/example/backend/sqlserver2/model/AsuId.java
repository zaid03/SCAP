package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AsuId implements Serializable {
    private Integer ENT;
    private String AFACOD;
    private String ASUCOD; 

    public AsuId() {}

    public AsuId(Integer ENT, String AFACOD, String ASUCOD) {
        this.ENT = ENT;
        this.AFACOD = AFACOD;
        this.ASUCOD = ASUCOD;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getAFACOD() { return AFACOD; }
    public void setAFACOD(String AFACOD) { this.AFACOD = AFACOD; }

    public String getASUCOD() { return ASUCOD; }
    public void setASUCOD(String ASUCOD) { this.ASUCOD = ASUCOD; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsuId asuId = (AsuId) o;
        return Objects.equals(ENT, asuId.ENT) &&
               Objects.equals(AFACOD, asuId.AFACOD) &&
               Objects.equals(ASUCOD, asuId.ASUCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, AFACOD, ASUCOD);
    }
}
