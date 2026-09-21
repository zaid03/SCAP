package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class FdtId  implements Serializable {
    private Integer ENT;
    private String EJE;
    private Integer FACNUM;
    private Integer FDTLIN;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FdtId fdtId = (FdtId) o;
        return Objects.equals(ENT, fdtId.ENT) &&
               Objects.equals(EJE, fdtId.EJE) &&
               Objects.equals(FACNUM, fdtId.FACNUM) &&
               Objects.equals(FDTLIN, fdtId.FDTLIN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, FACNUM, FDTLIN);
    }
}
