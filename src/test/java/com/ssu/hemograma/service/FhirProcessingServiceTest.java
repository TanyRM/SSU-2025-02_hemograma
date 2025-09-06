package com.ssu.hemograma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FhirProcessingServiceTest {

    private FhirProcessingService fhirProcessingService;

    @BeforeEach
    void setUp() {
        fhirProcessingService = new FhirProcessingService();
    }

    private final String validFhirBundleJson = """
    {
      "resourceType": "Bundle",
      "meta": { "profile": ["https://fhir.saude.go.gov.br/r4/exame/StructureDefinition/hemograma"] },
      "identifier": { "value": "test-id" },
      "type": "collection",
      "entry": [
        {
          "fullUrl": "urn:uuid:c47949a4-22eb-4f2a-8e7b-2f81c9729598",
          "resource": {
            "resourceType": "Observation",
            "id": "exame-composto",
            "status": "final",
            "code": { "text": "Hemograma completo" }
          }
        }
      ]
    }
    """;

    @Test
    void deveProcessarPayloadFhirValidoSemLancarExcecao() {
        // A prova de conceito é que nenhuma exceção é lançada.
        assertDoesNotThrow(() -> {
            fhirProcessingService.processFhirPayload(validFhirBundleJson);
        });
    }
}