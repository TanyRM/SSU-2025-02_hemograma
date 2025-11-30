package com.ssu.hemograma.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.ssu.hemograma.model.Hemograma;
import com.ssu.hemograma.repository.HemogramaRepository;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.time.Period;


@Service
public class HemogramaService {

    private static final Logger log = LoggerFactory.getLogger(HemogramaService.class);

    private final HemogramaRepository hemogramaRepository;
    private final FhirContext fhirContext = FhirContext.forR4();

    public HemogramaService(HemogramaRepository hemogramaRepository) {
        this.hemogramaRepository = hemogramaRepository;
    }

    @Transactional
    public Hemograma processarBundle(String bundleJson) {
        log.info("📦 Processando Bundle FHIR recebido...");
        try {
            IParser parser = fhirContext.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, bundleJson);

            String bundleId = extrairBundleId(bundle);
            Optional<Hemograma> existente = hemogramaRepository.findByBundleId(bundleId);
            if (existente.isPresent()) {
                log.warn("⚠️ Bundle {} já processado anteriormente.", bundleId);
                return existente.get();
            }

            Hemograma hemograma = extrairDadosHemograma(bundle, bundleJson);
            validarCamposObrigatorios(hemograma);

            Hemograma salvo = hemogramaRepository.save(hemograma);
            log.info("💾 Hemograma salvo parcialmente. ID = {}", salvo.getId());

            boolean alertaColetivo = detectarAlertaSurto();
            salvo.setAlertaAnemia(alertaColetivo);
            salvo = hemogramaRepository.save(salvo);
            log.info("✅ Hemograma salvo com sucesso com alerta coletivo = {}", alertaColetivo);
            return salvo;

        } catch (Exception e) {
            log.error("❌ Erro ao processar bundle FHIR: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar bundle: " + e.getMessage(), e);
        }
    }

    // ===================== EXTRAÇÃO PRINCIPAL =====================
    public Hemograma extrairDadosHemograma(Bundle bundle, String bundleJson) throws Exception {
        // --- 1. Extração do Recurso Patient (CPF e Data de Nascimento) ---
        Patient patient = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Patient"))
                .map(e -> (Patient) e.getResource())
                .findFirst()
                .orElseThrow(() -> new Exception("Recurso Patient não encontrado. CPF e Data de Nascimento são obrigatórios."));

        String cpf = patient.getIdentifier().stream()
                .filter(i -> i.getSystem().toLowerCase().contains("cpf"))
                .map(Identifier::getValue)
                .findFirst()
                .orElseThrow(() -> new Exception("CPF não encontrado no Patient.identifier."));

        LocalDate dataNascimento = patient.getBirthDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // --- 2. Extração de valores laboratoriais (Observation) ---
        final String LOINC_HB = "718-7"; // Hemoglobina
        final String LOINC_HT = "4544-3"; // Hematócrito
        final String LOINC_HE = "789-8"; // Hemácias

        LocalDateTime dataColeta = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Observation"))
                .map(e -> ((Observation) e.getResource()).getEffectiveDateTimeType().getValue())
                .filter(d -> d != null)
                .map(d -> LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()))
                .findFirst()
                .orElse(LocalDateTime.now());

        // Função utilitária para extrair o valor de uma Observation pelo código LOINC
        Function<String, BigDecimal> extractValue = (loincCode) ->
                bundle.getEntry().stream()
                        .filter(e -> e.getResource() instanceof Observation)
                        .map(e -> (Observation) e.getResource())
                        .filter(o -> o.getCode().getCoding().stream().anyMatch(c -> loincCode.equals(c.getCode())))
                        .map(o -> (Quantity) o.getValue())
                        .map(Quantity::getValue)
                        .findFirst()
                        .orElse(null);

        // --- 3. Mapeamento para a entidade Hemograma ---
        Hemograma h = new Hemograma();
        h.setBundleId(extrairBundleId(bundle));
        h.setBundleJson(bundleJson);
        h.setPacienteCpf(cpf);
        h.setDataNascimento(dataNascimento);
        h.setDataColeta(dataColeta);
        h.setDataRecebimento(LocalDateTime.now());
        h.setIdadeEmMeses(calcularIdadeEmMeses(dataNascimento, dataColeta.toLocalDate()));

        h.setHemoglobina(extractValue.apply(LOINC_HB));
        h.setHematocrito(extractValue.apply(LOINC_HT));
        h.setHemacias(extractValue.apply(LOINC_HE));

        String classificacao = classificarHemograma(h);
        h.setClassificacaoAnemia(classificacao);
        boolean temAnemia = !classificacao.equals("normal") && !classificacao.equals("indefinido");

        h.setAnemia(temAnemia);
        h.setAlertaSurtoAcionado(temAnemia);

        // --- 4. Extração do Laboratório (Organization) ---
        Optional<Organization> organizationOpt = bundle.getEntry().stream()
                .filter(e -> e.getResource().getResourceType().name().equals("Organization"))
                .map(e -> (Organization) e.getResource())
                .findFirst();

        String cnes = organizationOpt
                .flatMap(org -> org.getIdentifier().stream()
                        .filter(i -> i.getSystem().contains("cnes") || i.getSystem().contains("cnpj"))
                        .map(Identifier::getValue)
                        .findFirst())
                .orElse("");

        h.setLaboratorioCnes(cnes);

        return h;
    }

    // ===================== MÉTODOS AUXILIARES =====================
    private void validarCamposObrigatorios(Hemograma hemograma) {
        if (hemograma.getPacienteCpf() == null || hemograma.getPacienteCpf().isEmpty()) {
            throw new IllegalStateException("CPF do paciente não encontrado no bundle");
        }
        if (hemograma.getLaboratorioCnes() == null) {
            hemograma.setLaboratorioCnes("");
        }
        if (hemograma.getDataColeta() == null) {
            hemograma.setDataColeta(LocalDateTime.now());
        }
    }

    private String extrairBundleId(Bundle bundle) {
        if (bundle.hasIdentifier()) return bundle.getIdentifier().getValue();
        if (bundle.hasId()) return bundle.getId();
        return "BUNDLE-" + System.currentTimeMillis();
    }

    private int calcularIdadeEmMeses(LocalDate dataNascimento, LocalDate dataReferencia) {
        Period periodo = Period.between(dataNascimento, dataReferencia);
        return periodo.getYears() * 12 + periodo.getMonths();
    }


    private void processarObservations(Bundle bundle, Hemograma hemograma) {
        String cpf = null;
        String cnes = null;
        LocalDateTime dataColeta = null;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            if (resource instanceof Observation) {
                Observation obs = (Observation) resource;

                if (cpf == null && obs.hasSubject() && obs.getSubject().hasIdentifier()) {
                    cpf = obs.getSubject().getIdentifier().getValue();
                }

                if (cnes == null && obs.hasPerformer() && !obs.getPerformer().isEmpty()) {
                    Reference performer = obs.getPerformer().get(0);
                    if (performer.hasIdentifier()) {
                        cnes = performer.getIdentifier().getValue();
                    }
                }

                if (dataColeta == null) {
                    dataColeta = extrairDataColeta(obs);
                }

                processarObservation(obs, hemograma);
            }
        }

        if (cpf != null) {
            hemograma.setPacienteCpf(cpf);
        }
        if (cnes != null) {
            hemograma.setLaboratorioCnes(cnes);
        }
        if (dataColeta != null) {
            hemograma.setDataColeta(dataColeta);
        }
    }

    private LocalDateTime extrairDataColeta(Observation obs) {
        if (obs.hasEffectiveDateTimeType()) {
            try {
                Date data = obs.getEffectiveDateTimeType().getValue();
                return LocalDateTime.ofInstant(data.toInstant(), ZoneId.systemDefault());
            } catch (Exception e) {
                log.warn("Erro ao converter effectiveDateTime: {}", e.getMessage());
            }
        }

        if (obs.hasContained()) {
            for (Resource contained : obs.getContained()) {
                if (contained instanceof Specimen) {
                    Specimen specimen = (Specimen) contained;
                    if (specimen.hasCollection() && specimen.getCollection().hasCollectedDateTimeType()) {
                        try {
                            Date data = specimen.getCollection().getCollectedDateTimeType().getValue();
                            return LocalDateTime.ofInstant(data.toInstant(), ZoneId.systemDefault());
                        } catch (Exception e) {
                            log.warn("Erro ao converter collectedDateTime: {}", e.getMessage());
                        }
                    }
                }
            }
        }

        return null;
    }

    private void processarObservation(Observation obs, Hemograma hemograma) {
        if (!obs.hasCode() || !obs.hasValueQuantity()) {
            return;
        }

        String loincCode = extrairLoincCode(obs);
        double valor = obs.getValueQuantity().getValue().doubleValue();

        Double min = null;
        Double max = null;
        if (obs.hasReferenceRange() && !obs.getReferenceRange().isEmpty()) {
            Observation.ObservationReferenceRangeComponent range = obs.getReferenceRange().get(0);
            if (range.hasLow()) min = range.getLow().getValue().doubleValue();
            if (range.hasHigh()) max = range.getHigh().getValue().doubleValue();
        }

        switch (loincCode) {
            case "718-7": // Hemoglobina
                hemograma.setHemoglobina(BigDecimal.valueOf(valor));
                hemograma.setHemoglobinaMin(min);
                hemograma.setHemoglobinaMax(max);
                break;

            case "4544-3": // Hematócrito
                hemograma.setHematocrito(BigDecimal.valueOf(valor));
                break;

            case "789-8": // Hemácias
                hemograma.setHemacias(BigDecimal.valueOf(valor));
                break;

            case "6690-2": // Leucócitos totais
                hemograma.setLeucocitos(BigDecimal.valueOf(valor));
                break;
        }
    }

    private String extrairLoincCode(Observation obs) {
        if (obs.getCode().hasCoding()) {
            for (Coding coding : obs.getCode().getCoding()) {
                if ("http://loinc.org".equals(coding.getSystem())) {
                    return coding.getCode();
                }
            }
        }
        return "";
    }

    public boolean detectarAlertaSurto() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(24);
        long casos = this.contarCasosAnemia(inicio);
        return casos >= 5;
    }

    public String classificarAnemia(BigDecimal hb, int idadeMeses) {
        if (hb == null) return "indefinido";

        double valor = hb.doubleValue();

        // Crianças (6 meses a 5 anos)
        if (idadeMeses >= 6 && idadeMeses < 60) {
            if (valor < 7) return "grave";
            if (valor < 10) return "moderada";
            if (valor < 11) return "leve";
            return "normal";
        }

        // Crianças (5 a 11 anos)
        if (idadeMeses < 132) {
            if (valor < 8) return "grave";
            if (valor < 11) return "moderada";
            if (valor < 11.5) return "leve";
            return "normal";
        }

        // Adolescentes (12 a 14 anos)
        if (idadeMeses < 180) {
            if (valor < 8) return "grave";
            if (valor < 11.5) return "moderada";
            if (valor < 12) return "leve";
            return "normal";
        }

        return "normal";
    }

    public String classificarHemograma(Hemograma h) {
        String anemia = classificarAnemia(h.getHemoglobina(), h.getIdadeEmMeses());

        BigDecimal leuc = h.getLeucocitos();
        boolean leucAlterado = leuc != null && (leuc.doubleValue() < 4 || leuc.doubleValue() > 12);

        if ("grave".equals(anemia) || leucAlterado) {
            return "grave";
        } else if ("moderada".equals(anemia)) {
            return "moderada";
        } else if ("leve".equals(anemia)) {
            return "leve";
        }
        return "normal";
    }

    public List<Hemograma> listarTodos() {
        return hemogramaRepository.findAll();
    }

    public List<Hemograma> listarComAnemia(LocalDateTime apartirDe) {
        return hemogramaRepository.findHemogramasComAlertasAposData(apartirDe);
    }

    public Long contarCasosAnemia(LocalDateTime apartirDe) {
        return hemogramaRepository.contarCasosAnemiaAposData(apartirDe);
    }
}