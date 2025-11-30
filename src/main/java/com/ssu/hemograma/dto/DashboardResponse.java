package com.ssu.hemograma.dto;

import java.util.Map;
import java.util.List;

public record DashboardResponse(
        long totalHemogramas,
        long totalAnemia,
        long totalAlerta,
        long totalSurto,
        Map<String, Long> porClassificacao,
        Map<String, Long> porFaixaEtaria,
        List<TimelinePoint> timeline
) {}
