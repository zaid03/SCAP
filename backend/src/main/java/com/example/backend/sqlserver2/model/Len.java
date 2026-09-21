package com.example.backend.sqlserver2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "LEN", schema = "dbo")
public class Len {
    @Id
    private Integer LENCOD;

    private String LENDES;

    private String LENTXT;

    private LocalDateTime LENCAD;

    public Integer getLENCOD(){ return LENCOD; }
    public void setLENCOD(Integer LENCOD){ this.LENCOD = LENCOD; }

    public String getLENDES(){ return LENDES; }
    public void setLENDES(String LENDES) { this.LENDES = LENDES; }

    public String getLENTXT(){ return LENTXT; }
    public void setLENTXT(String LENTXT){ this.LENTXT = LENTXT; }

    public LocalDateTime getLENCAD(){return LENCAD; }
    public void setLENCAD(LocalDateTime LENCAD){ this.LENCAD = LENCAD; }
}