package com.example.backend.dto;

public interface CambiarADProjection {
    ConnProjection getConn();
    interface ConnProjection {
        Integer getCONCOD();
        String getCONLOT();
        String getCONDES();
    }
}