// src/main/java/com/ssu/hemograma/service/AnaliseService.java

package com.ssu.hemograma.service;

import com.ssu.hemograma.model.Hemograma;
import com.ssu.hemograma.repository.HemogramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnaliseService {

    @Autowired
    private HemogramaRepository repository;

    private static final int MAX_BUNDLE_SIZE = 1000000; // 1MB por exemplo

    // --- Regra de Negócio da OMS (Hb) ---
    private boolean isAnemia(int idadeEmMeses, BigDecimal hemoglobina) {

        BigDecimal limiteHb;

        // Crianças de 0 a 5 anos (até 60 meses)
        if (idadeEmMeses >= 6 && idadeEmMeses <= 60) {
            limiteHb = new BigDecimal("11.0");
        } else if (idadeEmMeses >= 2 && idadeEmMeses < 6) {
            limiteHb = new BigDecimal("9.5");
        } else if (idadeEmMeses >= 1 && idadeEmMeses < 2) {
            limiteHb = new BigDecimal("10.0");
        } else if (idadeEmMeses >= 0 && idadeEmMeses < 1) { // 0 a 1 mês
            limiteHb = new BigDecimal("13.0");
        } else {
            return false; // Fora do escopo de análise pediátrica
        }

        // Uso de compareTo: se o resultado for < 0, significa que hemoglobina < limiteHb
        return hemoglobina.compareTo(limiteHb) < 0;
    }

    public Hemograma analisarEAtualizar(Hemograma hemograma) {

        // 1. A Hemoglobina é o campo principal da análise.
        if (hemograma.getHemoglobina() != null) {
            boolean anemia = isAnemia(hemograma.getIdadeEmMeses(), hemograma.getHemoglobina());
            hemograma.setAnemia(anemia);
            hemograma.setClassificacaoAnemia(anemia ? "ANEMIA_DETECTADA" : "NORMAL");
        } else {
            hemograma.setClassificacaoAnemia("VALOR_AUSENTE");
        }

        // 2. Salva ou atualiza no banco
        return repository.save(hemograma);
    }
    // --- Lógica de Surto ---
    public String verificarSurto() {
        // Analisar os últimos 100 exames recebidos que estão na faixa de idade relevante (0-5 anos)
        final int LIMITE_ANALISE = 100;
        final double LIMIAR_SURTO = 0.30; // 30%

        // Usa o método otimizado do repositório
        List<Hemograma> ultimosExames = repository.findLastHemogramas(LIMITE_ANALISE);

        if (ultimosExames.isEmpty()) {
            return "Nenhum hemograma recente registrado para análise de surto.";
        }

        // Filtra apenas os exames de crianças entre 0 e 5 anos (0 a 60 meses) e que foram analisados.
        List<Hemograma> examesValidosParaSurto = ultimosExames.stream()
                .filter(h -> h.getIdadeEmMeses() >= 0 && h.getIdadeEmMeses() <= 60)
                .filter(h -> h.getClassificacaoAnemia() != null && !h.getClassificacaoAnemia().equals("VALOR_AUSENTE"))
                .toList();

        if (examesValidosParaSurto.isEmpty()) {
            return "Os últimos 100 exames não contêm dados de crianças entre 0 e 5 anos ou o valor de Hb estava ausente.";
        }

        long casosAnemia = examesValidosParaSurto.stream()
                .filter(Hemograma::isAnemia)
                .count();

        double percentualAnemia = (double) casosAnemia / examesValidosParaSurto.size();

        // Atualizar flag de surto para os exames atuais (exemplo simplificado: marcando todos)
        boolean emSurto = percentualAnemia > LIMIAR_SURTO;
        if (emSurto) {
            // Lógica para marcar os exames como contribuintes de surto, se necessário
            // ex: examesValidosParaSurto.forEach(h -> h.setAlertaSurtoAcionado(true));
            // ex: repository.saveAll(examesValidosParaSurto);
        }


        if (emSurto) {
            return String.format(
                    "🚨 ALERTA DE SURTO! %.1f%% (%d/%d) dos últimos exames válidos são de Anemia.",
                    percentualAnemia * 100, casosAnemia, examesValidosParaSurto.size());
        } else {
            return String.format(
                    "✅ Monitoramento normal. Taxa de Anemia: %.1f%% (%d/%d).",
                    percentualAnemia * 100, casosAnemia, examesValidosParaSurto.size());
        }
    }
}