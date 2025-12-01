package com.ssu.hemograma.controller;

import com.ssu.hemograma.service.FhirProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fhir")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final FhirProcessingService fhirService;

    @Autowired
    public SubscriptionController(FhirProcessingService fhirService) {
        this.fhirService = fhirService;
    }

    /**
     * Receber notificações FHIR via POST.
     *
     * @param payload o recurso FHIR em formato JSON.
     * @return Uma resposta HTTP 200 (OK) se a requisição for aceita.
     */
    @PostMapping("/subscription")
    public ResponseEntity<Void> receiveFhirNotification(@RequestBody String payload) {
        logger.info("Notificação recebida de /subscription.");

        // Processar o payload recebido.
        fhirService.processFhirPayload(payload);

        return ResponseEntity.ok().build();
    }
}
