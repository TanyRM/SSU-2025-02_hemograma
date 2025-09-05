package com.ssu.hemograma.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FhirProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FhirProcessingService.class);

    // Cria um contexto FHIR para a versão R4
    private final FhirContext fhirContext = FhirContext.forR4();

    /**
     * Processa o payload JSON FHIR.
     *
     * @param jsonPayload O corpo da requisição em formato String.
     */
    public void processFhirPayload(String jsonPayload) {
        // Cria um parser de JSON a partir do FHIR.
        IParser jsonParser = fhirContext.newJsonParser();

        try {
            // Faz o parse do JSON para um objeto do HAPI FHIR.
            Bundle bundle = jsonParser.parseResource(Bundle.class, jsonPayload);
            logger.info("Payload FHIR recebido e parseado.");

            logger.info("ID do Bundle: {}", bundle.getIdentifier().getValue());
            logger.info("Número de entradas no Bundle: {}", bundle.getEntry().size());

            // Busca o exame principal.
            bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(resource -> resource.getIdElement().getIdPart().equals("exame-composto"))
                    .findFirst()
                    .ifPresent(resource -> {
                        if (resource instanceof Observation observation) {
                            logger.info("Recurso encontrado");
                            logger.info("Status do exame: {}", observation.getStatus().getDisplay());
                            logger.info("Código do exame: {}", observation.getCode().getText());
                        }
                    });

        } catch (Exception e) {
            logger.error("Falha ao fazer o parse: {}", e.getMessage());
        }
    }
}