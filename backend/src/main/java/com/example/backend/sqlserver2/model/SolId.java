package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class SolId implements Serializable{
    private Integer ENT;
    private Integer SOLNUM;
    private Integer SOLSUB;

    public SolId() {}
    public SolId(Integer ENT, Integer SOLNUM, Integer SOLSUB) {
        this.ENT = ENT;
        this.SOLNUM = SOLNUM;
        this.SOLSUB = SOLSUB;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public Integer getSOLNUM() {return SOLNUM;}
    public void setSOLNUM(Integer SOLSUB) {this.SOLNUM = SOLNUM;}

    public Integer getSOLSUB() {return SOLSUB;}
    public void setSOLSUB(Integer SOLSUB) {this.SOLSUB = SOLSUB;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolId)) return false;
        SolId solId = (SolId) o;
        return Objects.equals(ENT, solId.ENT) && Objects.equals(SOLNUM, solId.SOLNUM) && Objects.equals(SOLSUB, solId.SOLSUB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, SOLNUM, SOLSUB);
    }
}