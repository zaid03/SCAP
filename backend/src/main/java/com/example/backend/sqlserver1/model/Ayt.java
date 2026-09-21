package com.example.backend.sqlserver1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "AYT", schema = "dbo")
public class Ayt {
    @Id
    private Integer ENTCOD;

    private String ENT_ORG;

    private String ENT_COD;

    public Integer getENTCOD() { return ENTCOD; }
    public void setENTCOD(Integer ENTCOD) { this.ENTCOD = ENTCOD; }

    public String getENT_ORG() { return ENT_ORG; }
    public void setENT_ORG(String ENT_ORG) { this.ENT_ORG = ENT_ORG; }

    public String getENT_COD() { return ENT_COD; }
    public void setENT_COD(String ENT_COD) { this.ENT_COD = ENT_COD; }
}