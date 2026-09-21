package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AprId implements Serializable {
    private Integer ENT;
    private Integer TERCOD;
    private String AFACOD;
    private String ASUCOD;
    private String ARTCOD;

    public AprId(){}
    public AprId(Integer ENT, Integer TERCOD, String AFACOD, String ASUCOD, String ARTCOD){
        this.ENT = ENT;
        this.TERCOD = TERCOD;
        this.AFACOD = AFACOD;
        this.ASUCOD = ASUCOD;
        this.ARTCOD = ARTCOD;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AprId aprId = (AprId) o;
        return Objects.equals(ENT, aprId.ENT) &&
               Objects.equals(TERCOD, aprId.TERCOD) &&
               Objects.equals(AFACOD, aprId.AFACOD) &&
               Objects.equals(ASUCOD, aprId.ASUCOD) &&
               Objects.equals(ARTCOD, aprId.ARTCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, TERCOD, AFACOD, ASUCOD, ARTCOD);
    }
}
