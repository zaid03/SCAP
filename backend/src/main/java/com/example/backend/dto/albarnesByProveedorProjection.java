package com.example.backend.dto;

import java.time.LocalDateTime;

public interface albarnesByProveedorProjection {
    Integer getALBNUM();
    String getALBREF();
    LocalDateTime getALBDAT();
    Double getALBBIM();
    LocalDateTime getALBFRE();
    String getDEPCOD();
    String getALBCOM();
    Integer getSOLNUM();
    Integer getSOLSUB();
    Integer getFACNUM();
    String getALBOBS();
    String getCONCTP();
    String getCONCPR();
    String getCONCCR();

    TerProjection getTer();

    interface TerProjection {
        Integer getTERCOD();
        String getTERNOM();
    }
}