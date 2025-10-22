package com.ssu.hemograma.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hemogramas", indexes = {
        @Index(name = "idx_data_coleta", columnList = "dataColeta"),
        @Index(name = "idx_alerta_anemia", columnList = "alertaAnemia")
})
public class Hemograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String bundleId;

    @Column(nullable = false, length = 14)
    private String pacienteCpf;

    @Column(nullable = false, length = 20)
    private String laboratorioCnes;

    @Column(nullable = false)
    private LocalDateTime dataColeta;

    @Column(nullable = false)
    private LocalDateTime dataRecebimento;

    // Valores dos exames principais para anemia
    private Double hemoglobina;
    private Double hematocrito;
    private Double hemacias;

    // Valores de referencia para hemoglobina
    private Double hemoglobinaMin;
    private Double hemoglobinaMax;

    // Flag de alerta - FOCO EM ANEMIA
    @Column(nullable = false)
    private Boolean alertaAnemia;

    // Classificacao da anemia
    @Column(length = 20)
    private String classificacaoAnemia; // LEVE, MODERADA, GRAVE

    // IMPORTANTE: Aumentar tamanho do campo para MEDIUMTEXT (16MB)
    @Column(columnDefinition = "MEDIUMTEXT")
    private String bundleJson;

    public Hemograma() {
        this.alertaAnemia = false;
    }

    @PrePersist
    protected void onCreate() {
        dataRecebimento = LocalDateTime.now();

        // Analise automatica de anemia
        alertaAnemia = verificarAnemia();
        if (alertaAnemia && hemoglobina != null) {
            classificacaoAnemia = classificarAnemia();
        }
    }

    /**
     * Verifica se ha anemia baseado na hemoglobina
     * Criterio: hemoglobina < 11.0 g/dL para criancas 1-5 anos (OMS)
     */
    private Boolean verificarAnemia() {
        if (hemoglobina == null) {
            return false;
        }
        // OMS: anemia em criancas 1-5 anos = Hb < 11.0 g/dL
        return hemoglobina < 11.0;
    }

    /**
     * Classifica o nivel da anemia para criancas de 1-5 anos
     * Baseado em criterios da OMS
     */
    private String classificarAnemia() {
        if (hemoglobina == null || hemoglobina >= 11.0) {
            return "SEM_ANEMIA";
        } else if (hemoglobina >= 10.0) {
            return "LEVE";
        } else if (hemoglobina >= 7.0) {
            return "MODERADA";
        } else {
            return "GRAVE";
        }
    }

    public Boolean possuiAlerta() {
        return alertaAnemia;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBundleId() { return bundleId; }
    public void setBundleId(String bundleId) { this.bundleId = bundleId; }

    public String getPacienteCpf() { return pacienteCpf; }
    public void setPacienteCpf(String pacienteCpf) { this.pacienteCpf = pacienteCpf; }

    public String getLaboratorioCnes() { return laboratorioCnes; }
    public void setLaboratorioCnes(String laboratorioCnes) { this.laboratorioCnes = laboratorioCnes; }

    public LocalDateTime getDataColeta() { return dataColeta; }
    public void setDataColeta(LocalDateTime dataColeta) { this.dataColeta = dataColeta; }

    public LocalDateTime getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(LocalDateTime dataRecebimento) { this.dataRecebimento = dataRecebimento; }

    public Double getHemoglobina() { return hemoglobina; }
    public void setHemoglobina(Double hemoglobina) { this.hemoglobina = hemoglobina; }

    public Double getHematocrito() { return hematocrito; }
    public void setHematocrito(Double hematocrito) { this.hematocrito = hematocrito; }

    public Double getHemacias() { return hemacias; }
    public void setHemacias(Double hemacias) { this.hemacias = hemacias; }

    public Double getHemoglobinaMin() { return hemoglobinaMin; }
    public void setHemoglobinaMin(Double hemoglobinaMin) { this.hemoglobinaMin = hemoglobinaMin; }

    public Double getHemoglobinaMax() { return hemoglobinaMax; }
    public void setHemoglobinaMax(Double hemoglobinaMax) { this.hemoglobinaMax = hemoglobinaMax; }

    public Boolean getAlertaAnemia() { return alertaAnemia; }
    public void setAlertaAnemia(Boolean alertaAnemia) { this.alertaAnemia = alertaAnemia; }

    public String getClassificacaoAnemia() { return classificacaoAnemia; }
    public void setClassificacaoAnemia(String classificacaoAnemia) { this.classificacaoAnemia = classificacaoAnemia; }

    public String getBundleJson() { return bundleJson; }
    public void setBundleJson(String bundleJson) { this.bundleJson = bundleJson; }
}