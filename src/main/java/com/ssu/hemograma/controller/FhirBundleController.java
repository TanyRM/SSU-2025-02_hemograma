package com.ssu.hemograma.controller;

import com.ssu.hemograma.model.Hemograma;
import com.ssu.hemograma.service.HemogramaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fhir")
public class FhirBundleController {

    private static final Logger log = LoggerFactory.getLogger(FhirBundleController.class);

    private final HemogramaService hemogramaService;

    public FhirBundleController(HemogramaService hemogramaService) {
        this.hemogramaService = hemogramaService;
    }

    /**
     * Endpoint para receber Bundles FHIR do gerador
     */
    @PostMapping(value = "/Bundle",
            consumes = {"application/fhir+json", "application/json"},
            produces = {"application/fhir+json", "application/json"})
    public ResponseEntity<Map<String, Object>> receberBundle(@RequestBody String bundleJson) {
        log.info("Bundle FHIR recebido");

        try {
            Hemograma hemograma = hemogramaService.processarBundle(bundleJson);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bundle processado com sucesso");
            response.put("hemogramaId", hemograma.getId());
            response.put("bundleId", hemograma.getBundleId());
            response.put("alertaAnemia", hemograma.getAlertaAnemia());
            response.put("classificacaoAnemia", hemograma.getClassificacaoAnemia());
            response.put("hemoglobina", hemograma.getHemoglobina());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {
            log.error("Erro ao processar bundle", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro ao processar bundle: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }

    /**
     * Endpoint para listar todos os hemogramas
     */
    @GetMapping("/hemogramas")
    public ResponseEntity<List<Hemograma>> listarHemogramas() {
        List<Hemograma> hemogramas = hemogramaService.listarTodos();
        return ResponseEntity.ok(hemogramas);
    }

    /**
     * Endpoint de notificação de surto de anemia.
     */
    @GetMapping("/hemogramas/anemia")
    public ResponseEntity<Map<String, Object>> listarHemogramasComAnemia(
            @RequestParam(required = false, defaultValue = "24") int ultimasHoras) {

        LocalDateTime dataInicio = LocalDateTime.now().minusHours(ultimasHoras);
        Long casosAnemia = hemogramaService.contarCasosAnemia(dataInicio);

        Map<String, Object> response = new HashMap<>();
        response.put("periodo", ultimasHoras + " horas");
        response.put("totalCasosAnemia", casosAnemia);

        return ResponseEntity.ok(response);
    }
}