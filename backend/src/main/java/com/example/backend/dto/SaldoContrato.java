package com.example.backend.dto;

public interface SaldoContrato {
    Integer getCONCOD();
    CotProjection getCot();
    CgeProjection getCge();
    String getCGECOD();
    String getCOGOPD();
    String getCOGOP2();
    Double getCOGIMP();
    Double getCOGIM2();
    Double getCOGIAP();

    interface CotProjection {
        ConnProjection getConn();
        TerProjection getTer();
        Integer getTERCOD();
    }

    interface ConnProjection {
        String getCONLOT();
        String getCONDES();
    }

    interface TerProjection {
        String getTERNOM();
        String getTERNIF();
    }

    interface CgeProjection {
        String getCGEDES();
    }
}