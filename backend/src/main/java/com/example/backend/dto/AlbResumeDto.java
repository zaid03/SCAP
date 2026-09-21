package com.example.backend.dto;

import java.time.LocalDateTime;
import com.example.backend.sqlserver2.model.Alb;

public record AlbResumeDto(
        Integer albnun,
        String albref,
        LocalDateTime albdat,
        Double albbim,
        Integer solnum,
        Integer solsub,
        String albobs
) {
    public static AlbResumeDto from(Alb entity) {
        return new AlbResumeDto(
                entity.getALBNUM(),
                entity.getALBREF(),
                entity.getALBDAT(),
                entity.getALBBIM(),
                entity.getSOLNUM(),
                entity.getSOLSUB(),
                entity.getALBOBS()
        );
    }
}
