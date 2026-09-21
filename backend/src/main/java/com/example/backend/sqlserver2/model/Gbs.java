package com.example.backend.sqlserver2.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;

@Entity
@IdClass(GbsId.class)
@Table(name = "GBS", schema = "dbo")
public class Gbs {
    @Id
    @Column(nullable = false)
    private Integer ENT;

    @Id
    @Column(nullable = false)
    private String EJE;

    @Id
    @Column(nullable = false)
    private String CGECOD;

    @Id
    @Column(nullable = false)
    private String GBSREF;

    @Column(nullable = true)
    private String GBSOPE;

    @Column(nullable = true)
    private String GBSORG;

    @Column(nullable = true)
    private String GBSFUN;

    @Column(nullable = true)
    private String GBSECO;

    @Column(nullable = true)
    private String GBSSUB;

    @Column(nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime GBSFOP;

    @Column(nullable = true)
    private Double GBSIMP;

    @Column(nullable = true)
    private Double GBSIUS;

    @Column(nullable = true)
    private Double GBSICO;

    @Column(nullable = true)
    private Double GBSIUT;

    @Column(nullable = true)
    private Double GBSICT;

    @Column(nullable = true)
    private Double GBSIBG;

    @Column(nullable = true)
    private Double GBS413;

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getEJE() { return EJE; }
    public void setEJE(String EJE){ this.EJE = EJE; }

    public String getCGECOD() { return CGECOD; }    
    public void setCGECOD(String CGECOD) { this.CGECOD = CGECOD; }

    public String getGBSREF() { return GBSREF; }
    public void setGBSREF(String GBSREF) { this.GBSREF = GBSREF; }

    public String getGBSOPE() { return GBSOPE; }
    public void setGBSOPE(String GBSOPE) { this.GBSOPE = GBSOPE; }

    public String getGBSORG() { return GBSORG; }
    public void setGBSORG(String GBSORG) { this.GBSORG = GBSORG; }

    public String getGBSFUN() { return GBSFUN; }
    public void setGBSFUN(String GBSFUN) { this.GBSFUN = GBSFUN; }

    public String getGBSECO() { return GBSECO; }
    public void setGBSECO(String GBSECO) { this.GBSECO = GBSECO; }

    public String getGBSSUB() { return GBSSUB; }
    public void setGBSSUB(String GBSSUB) { this.GBSSUB = GBSSUB; }

    public LocalDateTime getGBSFOP() { return GBSFOP; }
    public void setGBSFOP(LocalDateTime GBSFOP) { this.GBSFOP = GBSFOP; }

    public Double getGBSIMP() { return GBSIMP; }
    public void setGBSIMP(Double GBSIMP) { this.GBSIMP = GBSIMP; }

    public Double getGBSIUS() { return GBSIUS; }
    public void setGBSIUS(Double GBSIUS) { this.GBSIUS = GBSIUS; }

    public Double getGBSICO() { return GBSICO; }
    public void setGBSICO(Double GBSICO) { this.GBSICO = GBSICO; }

    public Double getGBSIUT() { return GBSIUT; }
    public void setGBSIUT(Double GBSIUT) { this.GBSIUT = GBSIUT; }

    public Double getGBSICT() { return GBSICT; }
    public void setGBSICT(Double GBSICT) { this.GBSICT = GBSICT; }

    public Double getGBSIBG() { return GBSIBG; }
    public void setGBSIBG(Double GBSIBG) { this.GBSIBG = GBSIBG; }
    
    public Double getGBS413() { return GBS413; }
    public void setGBS413(Double GBS413) { this.GBS413 = GBS413; }
}
