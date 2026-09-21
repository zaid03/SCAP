package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class AlbId implements Serializable {
    private Integer ENT;
    private Integer ALBNUM;

    public AlbId() {}
    public AlbId(Integer ENT, Integer ALBNUM) {
        this.ENT = ENT;
        this.ALBNUM = ALBNUM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbId albId = (AlbId) o;
        return Objects.equals(ENT, albId.ENT) &&
               Objects.equals(ALBNUM, albId.ALBNUM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, ALBNUM);
    }
}
