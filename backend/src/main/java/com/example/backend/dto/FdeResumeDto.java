package com.example.backend.dto;

import com.example.backend.sqlserver2.model.Fde;

public record FdeResumeDto(
    String FDEREF,
    String FDEECO,
    Double FDEIMP,
    Double FDEDIF
) {
    public static FdeResumeDto from(Fde entity) {
        return new FdeResumeDto(
            entity.getFDEREF(),
            entity.getFDEECO(),
            entity.getFDEIMP(),
            entity.getFDEDIF()
        );
    }
}