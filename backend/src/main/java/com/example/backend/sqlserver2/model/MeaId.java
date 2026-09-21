package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class MeaId {
    private Integer ENT;
    private Integer MAGCOD;
    private String AFACOD;
    private String ASUCOD;
    private String ARTCOD;

    public MeaId() {}

    public MeaId(Integer ENT, Integer MAGCOD, String AFACOD, String ASUCOD, String ARTCOD) {
        this.ENT = ENT;
        this.MAGCOD = MAGCOD;
        this.AFACOD = AFACOD;
        this.ASUCOD = ASUCOD;
        this.ARTCOD = ARTCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getMAGCOD() {return MAGCOD;}
    public void setMAGCOD(Integer MAGCOD) {this.MAGCOD = MAGCOD;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;} 

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeaId meaId = (MeaId) o;
        return Objects.equals(ENT, meaId.ENT) &&
               Objects.equals(MAGCOD, meaId.MAGCOD) &&
               Objects.equals(AFACOD, meaId.AFACOD) &&
               Objects.equals(ASUCOD, meaId.ASUCOD) &&
               Objects.equals(ARTCOD, meaId.ARTCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, MAGCOD, AFACOD, ASUCOD);
    }
}
