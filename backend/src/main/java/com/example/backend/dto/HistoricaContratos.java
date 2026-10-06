package com.example.backend.dto;

import java.util.List;
import java.time.LocalDateTime;

public interface HistoricaContratos {
    Integer getCONCOD();
    String getCGECOD();
    String getCOHOPD();
    String getCOHRFD();
    LocalDateTime getCOHFEC();

    ConnProjection getCon();
    CgeProjection getCge();

    default String getBloqueado() {
        Integer bloqueado = getCon() == null ? null : getCon().getCONBLO();
        return Integer.valueOf(1).equals(bloqueado) ? "Sí" : "No";
    }

    interface ConnProjection {
        String getCONLOT();
        String getCONDES();
        Integer getCONBLO();
        List<CotProjection> getCots();
    }

    interface CotProjection {
        Integer getTERCOD();
        TerProjection getTer();
    }

    interface TerProjection {
        String getTERNOM();
        String getTERNIF();
    }

    interface CgeProjection {
        String getCGECOD();
        String getCGEDES();
    }
}