package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(MagId.class)
@Table(name = "MAG", schema = "dbo")
public class Mag {
    @Id
    private Integer ENT;

    @Id
    private Integer MAGCOD;

    private String DEPCOD;

    private String MAGNOM;

    private String MAGDOM;

    private String MAGCPO;

    private String MAGTEL;

    private String MAGFAX;

    private String  MAGCOE;

    private String MAGOBS;

    private String PAICOD;

    private String PROCOD;

    private String MUNCOD;

    private String PERCOD;

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public Integer getMAGCOD() { return MAGCOD; }
    public void setMAGCOD(Integer MAGCOD) { this.MAGCOD = MAGCOD; }

    public String getDEPCOD() { return DEPCOD; }
    public void setDEPCOD(String DEPCOD) { this.DEPCOD = DEPCOD; }

    public String getMAGNOM() { return MAGNOM; }
    public void setMAGNOM(String MAGNOM) { this.MAGNOM = MAGNOM; }

    public String getMAGDOM() { return MAGDOM; }
    public void setMAGDOM(String MAGDOM) { this.MAGDOM = MAGDOM; }

    public String getMAGCPO() { return MAGCPO; }
    public void setMAGCPO(String MAGCPO) { this.MAGCPO = MAGCPO; }

    public String getMAGTEL() { return MAGTEL; }
    public void setMAGTEL(String MAGTEL) { this.MAGTEL = MAGTEL; }

    public String getMAGFAX() { return MAGFAX; }
    public void setMAGFAX(String MAGFAX) { this.MAGFAX = MAGFAX; }

    public String getMAGCOE() { return MAGCOE; }
    public void setMAGCOE(String MAGCOE) { this.MAGCOE = MAGCOE; }

    public String getMAGOBS() { return MAGOBS; }
    public void setMAGOBS(String MAGOBS) { this.MAGOBS = MAGOBS; }

    public String getPAICOD() { return PAICOD; }
    public void setPAICOD(String PAICOD) { this.PAICOD = PAICOD; }

    public String getPROCOD() { return PROCOD; }
    public void setPROCOD(String PROCOD) { this.PROCOD = PROCOD; }

    public String getMUNCOD() { return MUNCOD; }
    public void setMUNCOD(String MUNCOD) { this.MUNCOD = MUNCOD; }

    public String getPERCOD() { return PERCOD; }
    public void setPERCOD(String PERCOD) { this.PERCOD = PERCOD; }
}
