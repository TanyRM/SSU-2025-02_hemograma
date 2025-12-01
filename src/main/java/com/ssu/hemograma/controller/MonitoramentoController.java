package com.ssu.hemograma.controller;

import com.ssu.hemograma.model.Hemograma;
import com.ssu.hemograma.service.AnaliseService;
import com.ssu.hemograma.service.HemogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitoramento")
public class MonitoramentoController {

    private final AnaliseService analiseService;
    private final HemogramaService hemogramaService;

    public MonitoramentoController(AnaliseService analiseService, HemogramaService hemogramaService) {
        this.analiseService = analiseService;
        this.hemogramaService = hemogramaService;
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
        Long casosAnemia = analiseService.contarCasosAnemia(dataInicio);

        Map<String, Object> response = new HashMap<>();
        response.put("periodo", ultimasHoras + " horas");
        response.put("totalCasosAnemia", casosAnemia);

        return ResponseEntity.ok(response);
    }
}