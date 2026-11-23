package com.example.backend.dto;

import java.time.LocalDateTime;

public record AlbaranConsultaDto (
    Integer albnum,
    String albref,
    LocalDateTime albdat,
    Double albbim,
    LocalDateTime albfre,
    String depcod,
    String albcom,
    Integer solnum,
    Integer solsub,
    Integer facnum,
    String albobs,
    String conctp,
    String concpr,
    String conccr,
    String EJE,
    TerDto ter
) {

    public record TerDto(
        Integer tercod,
        String ternom,
        String ternif
    ) {}
}