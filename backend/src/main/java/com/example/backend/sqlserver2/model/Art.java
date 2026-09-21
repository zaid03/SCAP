package com.example.backend.sqlserver2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(ArtId.class)
@Table(name = "ART", schema = "dbo")
public class Art {
    
    @Id
    @Column(nullable = false)
    private Integer ENT;

    @Id
    @Column(nullable = false)
    private String AFACOD;

    @Id
    @Column(nullable = false)
    private String ASUCOD;

    @Id
    @Column(nullable = false)
    private String ARTCOD;

    @Column(nullable = true)
    private String ARTDES;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "ENT", referencedColumnName = "ENT", insertable = false, updatable = false),
        @JoinColumn(name = "AFACOD", referencedColumnName = "AFACOD", insertable = false, updatable = false),
        @JoinColumn(name = "ASUCOD", referencedColumnName = "ASUCOD", insertable = false, updatable = false)
    })
    private Asu asu;

    public Asu getAsu() { return asu; }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    public String getARTDES() {return ARTDES;}
    public void setARTDES(String ARTDES) {this.ARTDES = ARTDES;}
}
