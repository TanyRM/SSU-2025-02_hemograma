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

            Hemograma hemograma = new Hemograma();
            hemograma.setBundleId(bundleId);
            hemograma.setBundleJson(bundleJson);

            processarObservations(bundle, hemograma);

            validarCamposObrigatorios(hemograma);

            Hemograma salvo = hemogramaRepository.save(hemograma);

            // Se possui alerta de anemia, notifica sistema de analise
            if (salvo.getAlertaAnemia()) {
                notificarSistemaAnalise(salvo);
            }

            return salvo;

        } catch (Exception e) {
            log.error("Erro ao processar bundle FHIR", e);
            throw new RuntimeException("Erro ao processar bundle: " + e.getMessage(), e);
        }
    }

    private void validarCamposObrigatorios(Hemograma hemograma) {
        if (hemograma.getPacienteCpf() == null || hemograma.getPacienteCpf().isEmpty()) {
            throw new IllegalStateException("CPF do paciente não encontrado no bundle");
        }
        if (hemograma.getLaboratorioCnes() == null || hemograma.getLaboratorioCnes().isEmpty()) {
            throw new IllegalStateException("CNES do laboratório não encontrado no bundle");
        }
        if (hemograma.getDataColeta() == null) {
            // Se não encontrou data de coleta, usa data atual
            hemograma.setDataColeta(LocalDateTime.now());
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

        // Mapear valores por codigo LOINC
        switch (loincCode) {
            case "718-7": // Hemoglobina - Principal indicador
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