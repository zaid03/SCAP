package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AdeId implements Serializable {
    private Integer ENT;
    private Integer ALBNUM;
    private Integer ADENUM;

    public AdeId() {}
    public AdeId(Integer ENT, Integer ALBNUM, Integer ADENUM) {
        this.ENT = ENT;
        this.ALBNUM = ALBNUM;
        this.ADENUM =  ADENUM;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getALBNUM() {return ALBNUM;}
    public void setALBNUM(Integer ALBNUM) {this.ALBNUM = ALBNUM;}

    public Integer getADENUM() {return ADENUM;}
    public void setADENUM(Integer ADENUM) {this.ADENUM = ADENUM;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdeId adeId = (AdeId) o;
        return Objects.equals(ENT, adeId.ENT) &&
               Objects.equals(ALBNUM, adeId.ALBNUM) &&
               Objects.equals(ADENUM, adeId.ADENUM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, ALBNUM, ADENUM);
    }
}
