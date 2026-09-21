package com.example.backend.sqlserver2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;

@Entity
@IdClass(AunId.class)
@Table(name = "AUN", schema = "dbo")
public class Aun {
    @Id
    private Integer ENT;

    @Id
    private String AUNCOD;

    private String AUNDES;

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getAUNCOD() {return AUNCOD;}
    public void setAUNCOD(String AUNCOD) {this.AUNCOD = AUNCOD;}

    public String getAUNDES() {return AUNDES;}
    public void setAUNDES(String AUNDES) {this.AUNDES = AUNDES;}
}
