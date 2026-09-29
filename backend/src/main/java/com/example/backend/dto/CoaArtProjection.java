package com.example.backend.dto;

public interface CoaArtProjection {
    ArtInfo getArt();
    Double getCOAPRE();

    interface ArtInfo {
        String getAFACOD();
        String getASUCOD();
        String getARTCOD();
        String getARTDES();
    }
}