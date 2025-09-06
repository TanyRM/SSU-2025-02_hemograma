package com.ssu.hemograma.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Conteúdo mínimo do JSON para o teste
    private final String sampleFhirBundle = """
    {
      "resourceType": "Bundle",
      "type": "collection",
      "entry": []
    }
    """;

    @Test
    void deveReceberNotificacaoFhirERetornarStatusOK() throws Exception {
        mockMvc.perform(post("/api/fhir/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleFhirBundle))
                .andExpect(status().isOk());
    }
}