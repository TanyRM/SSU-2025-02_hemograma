package com.ssu.hemograma.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private LocalDateTime dataRecebimento;

    @Column(name = "is_anemia", nullable = false)
    private boolean isAnemia = false;

    // Valores dos exames principais para anemia
    @Column(name = "hemoglobina", precision = 5, scale = 2)
    private BigDecimal hemoglobina;

    @Column(name = "hematocrito", precision = 5, scale = 2)
    private BigDecimal hematocrito;

    @Column(name = "hemacias", precision = 5, scale = 2)
    private BigDecimal hemacias;

    @Column(name = "leucocitos", precision = 5, scale = 2)
    private BigDecimal leucocitos;

    @Column(name = "idade_em_meses", nullable = false)
    private int idadeEmMeses;

    // Valores de referencia para hemoglobina
    private Double hemoglobinaMin;
    private Double hemoglobinaMax;

    // Classificacao da anemia
    @Column(name = "classificacao_anemia", length = 20)
    private String classificacaoAnemia; // LEVE, MODERADA, GRAVE

    // Flag de alerta - FOCO EM ANEMIA
    @Column(nullable = false)
    private Boolean alertaAnemia;

    @Column(name = "alerta_surto_acionado", nullable = false)
    private boolean alertaSurtoAcionado = false;

    // IMPORTANTE: Aumentar tamanho do campo para MEDIUMTEXT (16MB)
    @Column(columnDefinition = "MEDIUMTEXT")
    private String bundleJson;

    public Hemograma() {
        this.alertaAnemia = false;
    }

    public Boolean possuiAlerta() {
        return alertaAnemia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getPacienteCpf() {
        return pacienteCpf;
    }

    public void setPacienteCpf(String pacienteCpf) {
        this.pacienteCpf = pacienteCpf;
    }

    public String getLaboratorioCnes() {
        return laboratorioCnes;
    }

    public void setLaboratorioCnes(String laboratorioCnes) {
        this.laboratorioCnes = laboratorioCnes;
    }

    public LocalDateTime getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public boolean isAnemia() {
        return isAnemia;
    }

    public void setAnemia(boolean anemia) {
        isAnemia = anemia;
    }

    public BigDecimal getHemoglobina() {
        return hemoglobina;
    }

    public void setHemoglobina(BigDecimal hemoglobina) {
        this.hemoglobina = hemoglobina;
    }

    public BigDecimal getHematocrito() {
        return hematocrito;
    }

    public void setHematocrito(BigDecimal hematocrito) {
        this.hematocrito = hematocrito;
    }

    public BigDecimal getHemacias() {
        return hemacias;
    }

    public void setHemacias(BigDecimal hemacias) {
        this.hemacias = hemacias;
    }

    public int getIdadeEmMeses() {
        return idadeEmMeses;
    }

    public void setIdadeEmMeses(int idadeEmMeses) {
        this.idadeEmMeses = idadeEmMeses;
    }

    public Double getHemoglobinaMin() {
        return hemoglobinaMin;
    }

    public void setHemoglobinaMin(Double hemoglobinaMin) {
        this.hemoglobinaMin = hemoglobinaMin;
    }

    public Double getHemoglobinaMax() {
        return hemoglobinaMax;
    }

    public void setHemoglobinaMax(Double hemoglobinaMax) {
        this.hemoglobinaMax = hemoglobinaMax;
    }

    public String getClassificacaoAnemia() {
        return classificacaoAnemia;
    }

    public void setClassificacaoAnemia(String classificacaoAnemia) {
        this.classificacaoAnemia = classificacaoAnemia;
    }

    public Boolean getAlertaAnemia() {
        return alertaAnemia;
    }

    public void setAlertaAnemia(Boolean alertaAnemia) {
        this.alertaAnemia = alertaAnemia;
    }

    public boolean isAlertaSurtoAcionado() {
        return alertaSurtoAcionado;
    }

    public void setAlertaSurtoAcionado(boolean alertaSurtoAcionado) {
        this.alertaSurtoAcionado = alertaSurtoAcionado;
    }

    public String getBundleJson() {
        return bundleJson;
    }

    public void setBundleJson(String bundleJson) {
        this.bundleJson = bundleJson;
    }

    public BigDecimal getLeucocitos() {
        return leucocitos;
    }

    public void setLeucocitos(BigDecimal leucocitos) {
        this.leucocitos = leucocitos;
    }
}