package com.ssu.hemograma.service;

import com.ssu.hemograma.repository.HemogramaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnaliseService {

    private final HemogramaRepository repository;

    public AnaliseService(HemogramaRepository repository) {
        this.repository = repository;
    }

    public Long contarCasosAnemia(LocalDateTime apartirDe) {
        return repository.contarCasosAnemiaAposData(apartirDe);
    }
}