package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;

@Entity
@Table(name = "PER", schema = "dbo")
public class Per {
    @Id
    private String PERCOD;

    private String PERNOM;

    private String PERCOE;

    private String PERTEL;

    private String PERTMO;

    private String PERCAR;

    private String PEROBS;

    public String getPERCOD() { return PERCOD; }
    public void setPERCOD(String PERCOD) { this.PERCOD = PERCOD; }

    public String getPERNOM() { return PERNOM; }
    public void setPERNOM(String PERNOM) { this.PERNOM = PERNOM; }

    public String getPERCOE() { return PERCOE; }
    public void setPERCOE(String PERCOE) { this.PERCOE = PERCOE; }

    public String getPERTEL() { return PERTEL;}
    public void setPERTEL(String PERTEL) { this.PERTEL = PERTEL;}

    public String getPERTMO() { return PERTMO;}
    public void setPERTMO(String PERTMO) { this.PERTMO = PERTMO; }

    public String getPERCAR() { return PERCAR; }
    public void setPERCAR(String PERCAR) { this.PERCAR = PERCAR; }

    public String getPEROBS() { return PEROBS; }
    public void setPEROBS(String PEROBS) { this.PEROBS = PEROBS; }
}