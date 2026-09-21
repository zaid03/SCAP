package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class ArtId implements Serializable {
    private Integer ENT;
    private String AFACOD;
    private String ASUCOD;
    private String ARTCOD;

    public ArtId() {}
    public ArtId(Integer ENT, String AFACOD, String ASUCOD, String ARTCOD) {
        this.ENT = ENT;
        this.AFACOD = AFACOD;
        this.ASUCOD = ASUCOD;
        this.ARTCOD = ARTCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

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
        ArtId artId = (ArtId) o;
        return Objects.equals(ENT, artId.ENT) &&
               Objects.equals(AFACOD, artId.AFACOD) &&
               Objects.equals(ASUCOD, artId.ASUCOD) &&
               Objects.equals(ARTCOD, artId.ARTCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, AFACOD, ASUCOD, ARTCOD);
    }
}
