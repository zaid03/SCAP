package com.example.backend.dto;

import com.example.backend.sqlserver2.model.Fdt;

public record FdtResumeDto(
    String FDTARE,
    String FDTORG,
    String FDTFUN,
    String FDTECO,
    Double FDTBSE,
    Double FDTPRE,
    Double FDTDTO,
    String FDTTXT
) {
    public static FdtResumeDto from(Fdt entity) {
        return new FdtResumeDto(
            entity.getFDTARE(),
            entity.getFDTORG(),
            entity.getFDTFUN(),
            entity.getFDTECO(),
            entity.getFDTBSE(),
            entity.getFDTPRE(),
            entity.getFDTDTO(),
            entity.getFDTTXT()
        ); 
    }
}