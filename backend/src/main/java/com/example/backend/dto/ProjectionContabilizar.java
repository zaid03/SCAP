package com.example.backend.dto;

import java.time.LocalDateTime;

public interface ProjectionContabilizar {
    Integer getFACNUM();
    String getFDEREF();
    String getFDEOPE();
    String getFDEORG();
    String getFDEFUN();
    String getFDEECO();
    String getFDESUB();
    Double getFDEIMP();
    Double getFDEDIF();
    String getCGECOD();
    String getFACDOC();
    java.time.LocalDateTime getFACFCO();
    Integer getTERCOD();
    Double getFACIMP();
    Integer getFACANN();
    Integer getFACFAC();
    java.time.LocalDateTime getFACDAT();
    String getTERNOM();
    String getTERNIF();
}