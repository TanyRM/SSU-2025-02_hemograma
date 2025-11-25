package com.ssu.hemograma.dto;

import com.ssu.hemograma.model.Hemograma;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HemogramaDTO {

    private Long id;
    private String bundleId;
    private String pacienteCpf;
    private String laboratorioCnes;
    private LocalDateTime dataColeta;
    private LocalDateTime dataRecebimento;

    // Valores dos exames
    private BigDecimal hemoglobina;
    private BigDecimal hematocrito;
    private BigDecimal leucocitos;
    private BigDecimal hemacias;
    private BigDecimal plaquetas;

    // Alertas
    private Boolean alertaAnemia;
    private Boolean alertaLeucocitose;
    private Boolean alertaLeucopenia;
    private Boolean alertaPlaquetopenia;
    private Boolean possuiAlerta;

    // Informações adicionais
    private String classificacaoAnemia;
    private String nivelGravidade;

    public static HemogramaDTO fromEntity(Hemograma hemograma) {
        HemogramaDTO dto = HemogramaDTO.builder()
                .id(hemograma.getId())
                .bundleId(hemograma.getBundleId())
                .pacienteCpf(hemograma.getPacienteCpf())
                .laboratorioCnes(hemograma.getLaboratorioCnes())
                .dataColeta(hemograma.getDataColeta())
                .dataRecebimento(hemograma.getDataRecebimento())
                .hemoglobina(hemograma.getHemoglobina())
                .hematocrito(hemograma.getHematocrito())
                .hemacias(hemograma.getHemacias())
                .alertaAnemia(hemograma.getAlertaAnemia())
                .possuiAlerta(hemograma.possuiAlerta())
                .build();

        // Classificar anemia se presente
//        if (hemograma.getAlertaAnemia() && hemograma.getHemoglobina() != null) {
//            dto.setClassificacaoAnemia(classificarAnemia(hemograma.getHemoglobina()));
//            dto.setNivelGravidade(determinarGravidade(hemograma.getHemoglobina()));
//        }

        return dto;
    }

    /**
     * Classificação de anemia para crianças de 1-5 anos
     */
    private static String classificarAnemia(double hemoglobina) {
        if (hemoglobina >= 11.0) {
            return "Sem anemia";
        } else if (hemoglobina >= 10.0) {
            return "Anemia leve";
        } else if (hemoglobina >= 7.0) {
            return "Anemia moderada";
        } else {
            return "Anemia grave";
        }
    }

    private static String determinarGravidade(double hemoglobina) {
        if (hemoglobina >= 11.0) {
            return "NORMAL";
        } else if (hemoglobina >= 10.0) {
            return "LEVE";
        } else if (hemoglobina >= 7.0) {
            return "MODERADA";
        } else {
            return "GRAVE";
        }
    }
}