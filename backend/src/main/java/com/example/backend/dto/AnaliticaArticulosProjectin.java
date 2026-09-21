package com.example.backend.dto;

public interface AnaliticaArticulosProjectin {
    String getAFACOD();
    String getARTCOD();
    String getARTDES();
    
    AfaProjection getAfa();
    
    interface AfaProjection {
        String getAFADES();
    }
}