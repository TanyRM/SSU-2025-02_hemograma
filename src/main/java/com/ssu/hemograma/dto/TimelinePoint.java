package com.ssu.hemograma.dto;

public record TimelinePoint(
        String periodo, // ex: 2025-01
        Long total
) {}