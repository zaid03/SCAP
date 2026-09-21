package com.example.backend.dto;

public interface CogCgeProjection {
    String getCGECOD();
    CogCge getCge();
    String getCOGOPD();
    String getCOGRFD();
    Double getCOGIMP();
    String getCOGOP2();
    String getCOGRF2();
    Double getCOGIM2();
    Double getCOGIAP();

    interface CogCge {
        String getCGEDES();
        String getCGEORG();
        String getCGEFUN();
    }
}
