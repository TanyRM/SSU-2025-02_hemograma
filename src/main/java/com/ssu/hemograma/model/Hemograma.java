// src/main/java/com/ssu/hemograma/model/Hemograma.java

package com.ssu.hemograma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hemogramas") // Mapeia para o nome da tabela no script
@Data
public class Hemograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificadores
    @Column(name = "bundle_id", nullable = false, unique = true, length = 100)
    private String bundleId;

    @Column(name = "paciente_cpf", nullable = false, length = 14)
    private String pacienteCpf;

    @Column(name = "laboratorio_cnes", length = 20)
    private String laboratorioCnes; // Novo campo

    // Datas
    @Column(name = "data_coleta", nullable = false)
    private LocalDateTime dataColeta;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento; // Novo campo, crucial para cálculo de idade

    @Column(name = "data_recebimento", nullable = false)
    private LocalDateTime dataRecebimento = LocalDateTime.now(); // Timestamp de criação

    /// Valores Chave: Alterado de Double para BigDecimal
    // Garante que o JPA use o tipo DECIMAL(5, 2) no banco, resolvendo o erro.
    @Column(name = "hemoglobina", precision = 5, scale = 2)
    private BigDecimal hemoglobina;

    @Column(name = "hematocrito", precision = 5, scale = 2)
    private BigDecimal hematocrito;

    @Column(name = "hemacias", precision = 5, scale = 2)
    private BigDecimal hemacias;

    // Análise e Monitoramento
    @Column(name = "idade_em_meses", nullable = false)
    private int idadeEmMeses;

    @Column(name = "is_anemia", nullable = false)
    private boolean isAnemia = false;

    @Column(name = "classificacao_anemia", length = 20)
    private String classificacaoAnemia;

    @Column(name = "alerta_surto_acionado", nullable = false)
    private boolean alertaSurtoAcionado = false;



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

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public int getIdadeEmMeses() {
        return idadeEmMeses;
    }

    public void setIdadeEmMeses(int idadeEmMeses) {
        this.idadeEmMeses = idadeEmMeses;
    }

    public boolean isAnemia() {
        return isAnemia;
    }

    public void setAnemia(boolean anemia) {
        isAnemia = anemia;
    }

    public String getClassificacaoAnemia() {
        return classificacaoAnemia;
    }

    public void setClassificacaoAnemia(String classificacaoAnemia) {
        this.classificacaoAnemia = classificacaoAnemia;
    }

    public boolean isAlertaSurtoAcionado() {
        return alertaSurtoAcionado;
    }

    public void setAlertaSurtoAcionado(boolean alertaSurtoAcionado) {
        this.alertaSurtoAcionado = alertaSurtoAcionado;
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
}
