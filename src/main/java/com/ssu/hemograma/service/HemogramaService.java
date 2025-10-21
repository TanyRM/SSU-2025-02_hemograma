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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        log.info("Processando Bundle FHIR recebido");

        try {
            IParser parser = fhirContext.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, bundleJson);

            // Extrair dados do Bundle
            String bundleId = extrairBundleId(bundle);

            // Verificar se ja existe
            Optional<Hemograma> existente = hemogramaRepository.findByBundleId(bundleId);
            if (existente.isPresent()) {
                log.warn("Bundle {} ja processado anteriormente", bundleId);
                return existente.get();
            }

            // Construir entidade Hemograma
            Hemograma hemograma = new Hemograma();
            hemograma.setBundleId(bundleId);
            hemograma.setBundleJson(bundleJson);

            // Processar observations do bundle
            processarObservations(bundle, hemograma);

            // Salvar no banco
            Hemograma salvo = hemogramaRepository.save(hemograma);

            log.info("Hemograma processado e salvo - ID: {}, Anemia: {}",
                    salvo.getId(), salvo.getAlertaAnemia());

            // Se possui alerta de anemia, notificar sistema de analise
            if (salvo.getAlertaAnemia()) {
                notificarSistemaAnalise(salvo);
            }

            return salvo;

        } catch (Exception e) {
            log.error("Erro ao processar bundle FHIR", e);
            throw new RuntimeException("Erro ao processar bundle: " + e.getMessage(), e);
        }
    }

    private String extrairBundleId(Bundle bundle) {
        if (bundle.hasIdentifier()) {
            return bundle.getIdentifier().getValue();
        }
        if (bundle.hasId()) {
            return bundle.getId();
        }
        return "BUNDLE-" + System.currentTimeMillis();
    }

    private void processarObservations(Bundle bundle, Hemograma hemograma) {
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            if (resource instanceof Observation) {
                Observation obs = (Observation) resource;
                processarObservation(obs, hemograma);
            }
        }
    }

    private void processarObservation(Observation obs, Hemograma hemograma) {
        if (!obs.hasCode() || !obs.hasValueQuantity()) {
            return;
        }

        String loincCode = extrairLoincCode(obs);
        double valor = obs.getValueQuantity().getValue().doubleValue();

        // Extrair valores de referencia
        Double min = null;
        Double max = null;
        if (obs.hasReferenceRange() && !obs.getReferenceRange().isEmpty()) {
            Observation.ObservationReferenceRangeComponent range = obs.getReferenceRange().get(0);
            if (range.hasLow()) {
                min = range.getLow().getValue().doubleValue();
            }
            if (range.hasHigh()) {
                max = range.getHigh().getValue().doubleValue();
            }
        }

        // Extrair dados do paciente e laboratorio
        if (obs.hasSubject() && obs.getSubject().hasIdentifier()) {
            hemograma.setPacienteCpf(obs.getSubject().getIdentifier().getValue());
        }

        if (obs.hasPerformer() && !obs.getPerformer().isEmpty() &&
                obs.getPerformer().get(0).hasIdentifier()) {
            hemograma.setLaboratorioCnes(obs.getPerformer().get(0).getIdentifier().getValue());
        }

        // Extrair data de coleta
        if (obs.hasEffectiveDateTimeType()) {
            Date dataColeta = obs.getEffectiveDateTimeType().getValue();
            hemograma.setDataColeta(LocalDateTime.ofInstant(
                    dataColeta.toInstant(), ZoneId.systemDefault()));
        }

        // Mapear valores por codigo LOINC - FOCO EM ANEMIA
        switch (loincCode) {
            case "718-7": // Hemoglobina - PRINCIPAL INDICADOR DE ANEMIA
                hemograma.setHemoglobina(valor);
                hemograma.setHemoglobinaMin(min);
                hemograma.setHemoglobinaMax(max);
                log.debug("Hemoglobina: {} g/dL (ref: {}-{})", valor, min, max);
                break;

            case "4544-3": // Hematocrito - Secundario
                hemograma.setHematocrito(valor);
                log.debug("Hematocrito: {}%", valor);
                break;

            case "789-8": // Hemacias - Secundario
                hemograma.setHemacias(valor);
                log.debug("Hemacias: {} x10^6/uL", valor);
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

    private void notificarSistemaAnalise(Hemograma hemograma) {

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