package com.example.backend.service;

public interface CotContratoProjection {
    ConnInfo getConn();
    TerInfo getTer();

    interface ConnInfo {
        Integer getCONCOD();
        String getCONLOT();
        String getCONDES();
        java.time.LocalDateTime getCONFIN();
        java.time.LocalDateTime getCONFFI();
        Integer getCONBLO();
    }

    interface TerInfo {
        Integer getTERCOD();
        String getTERNOM();
    }
}
