// src/main/java/com/ssu/hemograma/controller/MonitoramentoController.java

package com.ssu.hemograma.controller;

import com.ssu.hemograma.model.Hemograma;
import com.ssu.hemograma.service.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Optional;

@RestController
@RequestMapping("/monitoramento")
public class MonitoramentoController {

    @Autowired
    private AnaliseService analiseService;

    // Converte FHIR R4 para Java Object
    private final FhirContext fhirContext = FhirContext.forR4();

    @PostMapping(path = "/hemograma", consumes = "application/fhir+json")
    public ResponseEntity<String> receberHemograma(@RequestBody String bundleJson) {
        try {
            // 1. Salvar o JSON bruto (bundleJson) no objeto Hemograma para auditoria
            Bundle bundle = (Bundle) fhirContext.newJsonParser().parseResource(bundleJson);

            Hemograma novoHemograma = extrairDadosHemograma(bundle, bundleJson);

            // 2. Analisar e Persistir
            Hemograma analise = analiseService.analisarEAtualizar(novoHemograma);

            String status = analise.isAnemia() ? "ANEMIA detectada." : "Hemoglobina normal.";
            System.out.println("✅ Hemograma processado. Resultado: " + status + " (Idade: " + analise.getIdadeEmMeses() + " meses)");

            return ResponseEntity.ok("Hemograma FHIR recebido e analisado: " + status);
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar Bundle FHIR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar o Bundle: " + e.getMessage());
        }
    }

    @GetMapping("/status-surto")

            public ResponseEntity<String> verificarStatusSurto() {
        String status = analiseService.verificarSurto();

        // Se a resposta indicar surto, retornar status HTTP 429 (Too Many Requests - Alerta de Limite Atingido)
        if (status.contains("ALERTA DE SURTO")) {
            return ResponseEntity.status(429).body(status);
        }

        return ResponseEntity.ok(status);
    }

    // --- Métodos de Extração ---

    private Hemograma extrairDadosHemograma(Bundle bundle, String bundleJson) throws Exception {

        // --- 1. Extração do Recurso Patient (CPF e Data de Nascimento) ---
        Patient patient = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Patient"))
                .map(e -> (Patient) e.getResource())
                .findFirst()
                .orElseThrow(() -> new Exception("Recurso Patient não encontrado. CPF e Data de Nascimento são obrigatórios."));

        String cpf = patient.getIdentifier().stream()
                .filter(i -> i.getSystem().contains("cpf")) // Assumindo que a URL contém 'cpf'
                .map(Identifier::getValue)
                .findFirst()
                .orElseThrow(() -> new Exception("CPF não encontrado no Patient.identifier."));

        LocalDate dataNascimento = patient.getBirthDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // --- 2. Extração dos Valores de Exame (Observation) ---

        // Mapeamento dos códigos LOINC para os campos da tabela
        final String LOINC_HB = "718-7"; // Hemoglobina
        final String LOINC_HT = "4544-3"; // Hematócrito
        final String LOINC_HE = "789-8"; // Hemácias

        // Data de Coleta (a data deve ser consistente em todas as observations, usando a primeira)
        LocalDateTime dataColeta = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Observation"))
                .map(e -> ((Observation) e.getResource()).getEffectiveDateTimeType().asStringValue())
                .map(LocalDateTime::parse)
                .findFirst()
                .orElseThrow(() -> new Exception("Data de coleta (EffectiveDateTime) não encontrada."));

        // Função utilitária para extrair o valor de uma Observation pelo código LOINC
        java.util.function.Function<String, BigDecimal> extractValue = (loincCode) -> // <-- Mudança aqui (retorno)
                bundle.getEntry().stream()
                        .filter(e -> e.getResource().getResourceType().name().equals("Observation"))
                        .map(e -> (Observation) e.getResource())
                        .filter(o -> o.getCode().getCoding().stream().anyMatch(c -> c.getCode().equals(loincCode)))
                        .map(o -> (Quantity) o.getValue())
                        .map(q -> q.getValue()) // <-- Correção: getValue() retorna BigDecimal
                        .findFirst()
                        .orElse(null);


        // --- 3. Mapear para a Entidade Hemograma ---
        Hemograma h = new Hemograma();

        // Cálculo de Idade
        h.setDataNascimento(dataNascimento);
        h.setIdadeEmMeses(calcularIdadeEmMeses(dataNascimento, dataColeta.toLocalDate()));

        // Identificadores e Datas
        h.setBundleId(bundle.getId());
        h.setPacienteCpf(cpf);
        h.setDataColeta(dataColeta);
        h.setDataRecebimento(LocalDateTime.now());

        // Valores de Exame
        h.setHemoglobina(extractValue.apply(LOINC_HB));
        h.setHematocrito(extractValue.apply(LOINC_HT));
        h.setHemacias(extractValue.apply(LOINC_HE));

        Optional<Organization> organizationOpt = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Organization"))
                .map(e -> (Organization) e.getResource())
                .findFirst();

        String cnes = organizationOpt
                .flatMap(org -> org.getIdentifier().stream()
                        .filter(i -> i.getSystem().contains("cnes") || i.getSystem().contains("cnpj")) // Filtra por sistemas de identificação relevantes
                        .map(Identifier::getValue)
                        .findFirst())
                .orElse(""); // <--- Define como string vazia se não for encontrado (RESOLVE O ERRO NOT NULL)

        h.setLaboratorioCnes(cnes); // ATRIBUI O CNES OU STRING VAZIA

        return h;
    }

    /**
     * Calcula a diferença entre duas datas em meses completos (ex: 1 ano e 3 meses = 15 meses).
     */
    private int calcularIdadeEmMeses(LocalDate dataNascimento, LocalDate dataReferencia) {
        Period periodo = Period.between(dataNascimento, dataReferencia);
        return periodo.getYears() * 12 + periodo.getMonths();
    }
}