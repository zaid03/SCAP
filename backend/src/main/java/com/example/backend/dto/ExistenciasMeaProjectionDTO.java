package com.example.backend.dto;

public class ExistenciasMeaProjectionDTO implements ExistenciasMeaProjection {
    private Integer MAGCOD;
    private String DEPCOD;
    private String DEPDES;
    private Double MEAUNI;
    private String MEALOC;

    public ExistenciasMeaProjectionDTO(Integer MAGCOD, String DEPCOD, String DEPDES, Double MEAUNI, String MEALOC) {
        this.MAGCOD = MAGCOD;
        this.DEPCOD = DEPCOD;
        this.DEPDES = DEPDES;
        this.MEAUNI = MEAUNI;
        this.MEALOC = MEALOC;
    }

    @Override
    public Integer getMAGCOD() { return MAGCOD; }

    @Override
    public String getDEPCOD() { return DEPCOD; }

    @Override
    public String getDEPDES() { return DEPDES; }

    @Override
    public Double getMEAUNI() { return MEAUNI; }

    @Override
    public String getMEALOC() { return MEALOC; }
}
