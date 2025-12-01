package com.ssu.hemograma.service;

import com.ssu.hemograma.dto.TimelinePoint;
import com.ssu.hemograma.repository.HemogramaDashboardRepository;
import org.springframework.stereotype.Service;
import com.ssu.hemograma.dto.DashboardResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final HemogramaDashboardRepository repo;

    public DashboardService(HemogramaDashboardRepository repo) {
        this.repo = repo;
    }

    public DashboardResponse getDashboard() {

        long totalHemogramas = repo.totalHemogramas();
        long totalAnemia = repo.totalAnemia();
        long totalAlerta = repo.totalAlerta();
        long totalSurto = repo.totalSurto();

        Map<String, Long> porClassificacao =
                repo.porClassificacao().stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> ((Number) row[1]).longValue()
                        ));

        Map<String, Long> porFaixa =
                repo.porFaixaEtaria().stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> ((Number) row[1]).longValue()
                        ));

        List<TimelinePoint> timeline =
                repo.timeline().stream()
                        .map(row -> new TimelinePoint(
                                (String) row[0],
                                ((Number) row[1]).longValue()
                        ))
                        .toList();

        return new DashboardResponse(
                totalHemogramas,
                totalAnemia,
                totalAlerta,
                totalSurto,
                porClassificacao,
                porFaixa,
                timeline
        );
    }
}